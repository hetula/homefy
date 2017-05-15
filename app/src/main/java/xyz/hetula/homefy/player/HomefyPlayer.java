/*
 * MIT License
 *
 * Copyright (c) 2017 Tuomo Heino
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package xyz.hetula.homefy.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import xyz.hetula.homefy.service.Homefy;

import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;

public class HomefyPlayer {
    private static final String TAG = "HomefyPlayer";

    private BecomingNoisyReceiver myNoisyAudioStreamReceiver = new BecomingNoisyReceiver();
    private Set<PlaybackListener> mPlaybackListeners = new HashSet<>();
    private Handler mHandler = new Handler();

    private final List<Song> mPlaylist;

    private Context mContext;
    private MediaPlayer mPlayer;
    private MediaSessionCompat mSession;
    private MediaControllerCompat mController;
    private PlaybackStateCompat.Builder mStateBuilder;
    private AudioManager.OnAudioFocusChangeListener afChangeListener = this::onAudioFocusChange;
    private WifiManager.WifiLock mWifiLock;

    private Song mNowPlaying;

    public HomefyPlayer(Context context) {
        mPlaylist = new ArrayList<>();

        mContext = context;
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        context.registerReceiver(myNoisyAudioStreamReceiver, intentFilter);

        mSession = new MediaSessionCompat(context, "Homefy Player");

        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mStateBuilder = new PlaybackStateCompat.Builder().setActions(
                PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mSession.setPlaybackState(mStateBuilder.build());
        mSession.setActive(true);

        mController = mSession.getController();

        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build());
        mPlayer.setOnCompletionListener(this::onPlayComplete);
        mPlayer.setOnPreparedListener(this::onPrepareComplete);
        mPlayer.setOnErrorListener(this::onError);
        mPlayer.setOnBufferingUpdateListener(this::onBuffering);
        mPlayer.setWakeMode(mContext.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        mWifiLock = ((WifiManager) mContext
                .getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "HomefyPlayerWifiLock");

        MediaSessionCompat.Callback callback = new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                pauseResume();
            }

            @Override
            public void onPause() {
                AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                pauseResume();
            }

            @Override
            public void onSkipToNext() {
                next();
            }

            @Override
            public void onSkipToPrevious() {
                previous();
            }

            @Override
            public void onStop() {
                AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                stop();
            }
        };
    }

    public MediaSessionCompat getSession() {
        return mSession;
    }

    public void release() {
        mPlaybackListeners.clear();

        mSession.release();
        mSession = null;

        mPlayer.release();
        mPlayer = null;

        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }

        // Just in case as Android does not behave nicely when unregistering non-registered
        // receivers. Should never happen, but it is better to catch it than crash whole app.
        try {
            mContext.unregisterReceiver(myNoisyAudioStreamReceiver);
        } catch (IllegalStateException ex) {
            Log.w(TAG, "Releasing unregistered Noisy Receiver!", ex);
        }
        myNoisyAudioStreamReceiver = null;
        mContext = null;
    }

    @Nullable
    public Song nowPlaying() {
        return mNowPlaying;
    }

    public void play(Song song, @Nullable List<Song> playlist) {
        if (setupPlay(song) && playlist != null) {
            mPlaylist.clear();
            mPlaylist.addAll(playlist);
        }
    }

    public void pauseResume() {
        if (mNowPlaying == null) return;
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            for (PlaybackListener listener : mPlaybackListeners) {
                listener.onSongPlay(nowPlaying(), PlaybackListener.STATE_PAUSE);
            }
        } else {
            mPlayer.start();
            for (PlaybackListener listener : mPlaybackListeners) {
                listener.onSongPlay(nowPlaying(), PlaybackListener.STATE_RESUME);
            }
        }
    }

    public boolean isPlaying() {
        return mNowPlaying != null && mPlayer.isPlaying();
    }

    public boolean isPaused() {
        return mNowPlaying != null && !mPlayer.isPlaying();
    }

    public void stop() {
        abandonAudioFocus();
        for (PlaybackListener listener : mPlaybackListeners) {
            listener.onSongPlay(nowPlaying(), PlaybackListener.STATE_STOP);
        }
        mPlayer.stop();
    }

    public void previous() {
    }

    public void next() {
        if (mPlaylist.isEmpty()) return;
        // Normal Playback mode implementation
        // This needs own classes/methods etc for proper impl
        // TODO Implement Playback modes
        int id = mPlaylist.indexOf(mNowPlaying);
        if (id == -1) return;
        id++;
        if (id >= mPlaylist.size()) return;
        setupPlay(mPlaylist.get(id));
    }

    private boolean setupPlay(Song song) {
        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        // Request audio focus for playback
        int result = am.requestAudioFocus(afChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.w(TAG, "No AudioFocus Granted!");
            return false;
        }
        try {
            mWifiLock.acquire();
            Uri uri = Uri.parse(Homefy.library().getPlayPath(song));
            mPlayer.reset();
            Map<String, String> headers = new HashMap<>();
            Homefy.protocol().addAuthHeader(headers);
            mPlayer.setDataSource(mContext, uri, headers);
            mPlayer.prepareAsync();

            mNowPlaying = song;
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error when playing", e);
            return false;
        }
    }

    private void onPrepareComplete(MediaPlayer mp) {
        mp.start();
        for (PlaybackListener listener : mPlaybackListeners) {
            listener.onSongPlay(nowPlaying(), PlaybackListener.STATE_PLAY);
        }
    }

    private void onPlayComplete(MediaPlayer mp) {
        abandonAudioFocus();
        next();
    }

    private boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "Playback Error: " + what + " extra: " + extra);
        abandonAudioFocus();
        for (PlaybackListener listener : mPlaybackListeners) {
            listener.onSongPlay(nowPlaying(), PlaybackListener.STATE_STOP);
        }
        return true;
    }

    private void onBuffering(MediaPlayer mediaPlayer, int i) {
        if (i < 100) return;
        Log.d(TAG, "Fully buffered!");
        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }
    }

    private void abandonAudioFocus() {
        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        am.abandonAudioFocus(afChangeListener);
        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }
    }

    private void onAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            mController.getTransportControls().pause();
            mHandler.postDelayed(mController.getTransportControls()::stop,
                    TimeUnit.SECONDS.toMillis(30));
        } else if (focusChange == AUDIOFOCUS_LOSS_TRANSIENT) {
            mController.getTransportControls().pause();
        } else if (focusChange == AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            mPlayer.setVolume(0.1f, 0.1f);
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            mPlayer.setVolume(1, 1);
            // TODO deside if continueing playback is wanted here.
            // ducking wont pause, so receiving messages doesn't cause problems
            // Phone calls etc can cause problems if headset is removed during call
            // after call end playback can possibly continue from wrong speaker.
        }
    }

    public void unregisterPlaybackListener(PlaybackListener mPlaybackListener) {
        mPlaybackListeners.remove(mPlaybackListener);
    }

    public void registerPlaybackListener(PlaybackListener mPlaybackListener) {
        mPlaybackListeners.add(mPlaybackListener);
    }

    private class BecomingNoisyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                if (mNowPlaying == null) return;
                mPlayer.pause();
            }
        }
    }
}
