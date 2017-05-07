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

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import xyz.hetula.homefy.service.Homefy;

public class HomefyPlayer {
    private static final String TAG = "HomefyPlayer";

    private final List<Song> mPlaylist;

    private Context mContext;
    private MediaPlayer mPlayer;
    private MediaSession mSession;

    private Song mNowPlaying;

    public HomefyPlayer(Context context) {
        mContext = context;
        mSession = new MediaSession(context, "Homefy Player");

        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build());
        mPlayer.setOnCompletionListener(this::onPlayComplete);
        mPlayer.setOnPreparedListener(this::onPrepareComplete);
        mPlayer.setOnErrorListener(this::onError);

        mPlaylist = new ArrayList<>();
    }

    public MediaSession getSession() {
        return mSession;
    }

    public void release() {
        mSession.release();
        mSession = null;

        mPlayer.release();
        mPlayer = null;

        mContext = null;
    }

    @Nullable
    public Song nowPlaying() {
        return mNowPlaying;
    }

    public void play(Song song, @Nullable List<Song> playlist) {
        if(setupPlay(song) && playlist != null) {
            mPlaylist.clear();
            mPlaylist.addAll(playlist);
        }
    }

    public void pauseResume() {
        if(mNowPlaying == null) return;
        if(mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.start();
        }
    }

    public void stop() {
        mPlayer.stop();
    }

    public void previous() {
    }

    public void next() {
        if(mPlaylist.isEmpty()) return;
        // Normal Playback mode implementation
        // This needs own classes/methods etc for proper impl
        // TODO Implement Playback modes
        int id = mPlaylist.indexOf(mNowPlaying);
        if(id == -1) return;
        id++;
        if(id >= mPlaylist.size()) return;
        setupPlay(mPlaylist.get(id));
    }

    private boolean setupPlay(Song song) {
        try {
            Uri uri = Uri.parse(Homefy.library().getPlayPath(song));
            mPlayer.reset();
            mPlayer.setDataSource(mContext, uri, new HashMap<>());
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
    }

    private void onPlayComplete(MediaPlayer mp) {
        next();
    }

    private boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "Playback Error: " + what +" extra: " + extra);
        return true;
    }
}
