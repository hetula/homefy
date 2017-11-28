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

package xyz.hetula.homefy.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.*
import android.media.MediaPlayer
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import xyz.hetula.homefy.library.HomefyLibrary
import xyz.hetula.homefy.service.protocol.HomefyProtocol
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
class HomefyPlayer(private val mProtocol: HomefyProtocol,
                   private val mLibrary: HomefyLibrary) {
    private val playNextThresholdMs = 500L
    private val mPlaybackListeners = HashSet<(Song?, Int, Int) -> Unit>()
    private val mPlayback = Playback()

    private var mNoisyAudioStreamReceiver: BecomingNoisyReceiver? = null
    private var mMediaSession: MediaSessionCompat? = null
    private var mController: MediaControllerCompat? = null
    private var mWifiLock: WifiManager.WifiLock? = null
    private var mPlayer: MediaPlayer? = null
    private var mContext: Context? = null

    private var afChangeListener = this::onAudioFocusChange
    private var mLastPlayPress = 0L
    private var mHasFocus = false
    private var mAudioFocusRequest: AudioFocusRequest? = null

    fun initalize(context: Context): MediaSessionCompat {
        Log.d(TAG, "initialize: Initializing!")
        val mediaSession = MediaSessionCompat(context, "Homefy Player")
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                pauseResume()
            }

            override fun onPause() {
                pauseResume()
            }

            override fun onSkipToNext() {
                next()
            }

            override fun onSkipToPrevious() {
                previous()
            }

            override fun onStop() {
                stop()
            }
        })

        val player = MediaPlayer()
        player.setAudioAttributes(AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build())

        player.setOnCompletionListener(this::onPlayComplete)
        player.setOnPreparedListener(this::onPrepareComplete)
        player.setOnErrorListener(this::onError)
        player.setOnBufferingUpdateListener(this::onBuffering)
        player.setWakeMode(context.applicationContext, PowerManager.PARTIAL_WAKE_LOCK)

        mWifiLock = (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "HomefyPlayerWifiLock")

        mContext = context
        mPlayer = player
        mMediaSession = mediaSession
        mController = mediaSession.controller

        val noisyAudioStreamReceiver = BecomingNoisyReceiver()
        val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        context.registerReceiver(noisyAudioStreamReceiver, intentFilter)
        mNoisyAudioStreamReceiver = noisyAudioStreamReceiver

        mediaSession.isActive = true
        updatePlaybackState(PlaybackStateCompat.STATE_NONE)

        return mediaSession
    }

    fun release(context: Context) {
        releaseBroadcastReceiver(context, mNoisyAudioStreamReceiver)
        mNoisyAudioStreamReceiver = null

        loseAudioFocus()
        mPlaybackListeners.clear()

        mPlayer?.release()
        mPlayer = null

        mMediaSession?.release()
        mMediaSession = null
        mController = null

        releaseWifiLock(mWifiLock)
        mWifiLock = null

        mContext = null
    }

    fun nowPlaying(): Song? {
        return mPlayback.getCurrent()
    }

    fun queue(song: Song) {
        mPlayback.queueSong(Collections.singletonList(song))
    }

    fun queue(song: List<Song>) {
        mPlayback.queueSong(song)
    }

    fun play(song: Song, playlist: ArrayList<Song>?) {
        val player = mPlayer ?: return
        if (setupPlay(player, song)) {
            if (playlist == null)
                mPlayback.playSong(song, ArrayList())
            else
                mPlayback.playSong(song, playlist)
        }
    }

    fun pauseResume() {
        if (mPlayback.isEmpty()) {
            return
        }
        if (shouldPlayNext()) {
            next()
            return
        }
        mLastPlayPress = SystemClock.elapsedRealtime()

        player {
            if (it.isPlaying) {
                pause()
            } else {
                play()
            }
        }
    }

    private fun shouldPlayNext(): Boolean {
        if (mLastPlayPress == 0L) {
            return false
        }
        val diff = SystemClock.elapsedRealtime() - mLastPlayPress
        return diff < playNextThresholdMs
    }

    private fun pause() {
        player {
            if (mPlayback.isEmpty() || !it.isPlaying) return
            it.pause()
            mPlaybackListeners.forEach { it(nowPlaying()!!, STATE_PAUSE, -1) }
            updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
        }
    }

    private fun play() {
        if (mPlayback.isEmpty()) return
        player {
            it.start()
            mPlaybackListeners.forEach { it(nowPlaying()!!, STATE_RESUME, -1) }
            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
        }
    }

    val isPlaying: Boolean
        get(): Boolean {
            val player = mPlayer ?: return false
            return !mPlayback.isEmpty() && player.isPlaying
        }

    val isPaused: Boolean
        get(): Boolean {
            val player = mPlayer ?: return false
            return !mPlayback.isEmpty() && !player.isPlaying
        }

    fun stop() {
        mPlaybackListeners.forEach { it(nowPlaying()!!, STATE_STOP, -1) }
        mPlayback.stop()
        mPlayer?.stop()
        updatePlaybackState(PlaybackStateCompat.STATE_STOPPED)
    }

    fun previous() {
        mPlayback.previous()
        if (mPlayback.isEmpty()) return
        updatePlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS)
        mPlayback.getCurrent { song ->
            player { setupPlay(it, song) }
        }
    }

    fun next() {
        mPlayback.next()
        if (mPlayback.isEmpty()) return
        updatePlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT)
        mPlayback.getCurrent { song ->
            player { setupPlay(it, song) }
        }
    }

    fun cyclePlaybackMode(): PlaybackMode {
        mPlayback.cyclePlaybackMode()
        return mPlayback.playbackMode
    }

    fun queryPosition(): Int {
        if (mPlayback.isEmpty()) return 0
        val player = mPlayer ?: return 0
        return player.currentPosition / 1000
    }

    fun seekTo(position: Int) {
        mPlayer?.seekTo(position * 1000)
    }

    private fun queryPosMs(): Long {
        if (mPlayback.isEmpty()) return 0L
        val player = mPlayer ?: return 0L
        return player.currentPosition.toLong()
    }

    private fun setupPlay(player: MediaPlayer, song: Song): Boolean {
        if (!mHasFocus) {
            tryGainAudioFocus()
            if (!mHasFocus) {
                return false
            }
        }
        Log.d(TAG, "Setuping new song to play!")
        return try {
            acquireWifiLock(mWifiLock)
            val uri = Uri.parse(mLibrary.getPlayPath(song))
            player.reset()
            val headers = HashMap<String, String>()
            mProtocol.addAuthHeader(headers)
            player.setDataSource(mContext, uri, headers)
            mMediaSession?.setMetadata(song.toMediaMetadata())
            updatePlaybackState(PlaybackStateCompat.STATE_CONNECTING)
            player.prepareAsync()
            true
        } catch (e: IOException) {
            Log.e(TAG, "Error when playing", e)
            releaseWifiLock(mWifiLock)
            false
        }
    }

    private fun onPrepareComplete(mp: MediaPlayer) {
        Log.d(TAG, "onPrepareComlete, Starting Playback")
        mp.start()
        mPlaybackListeners.forEach { it(nowPlaying()!!, STATE_PLAY, -1) }
        updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
    }

    private fun onPlayComplete(mp: MediaPlayer) {
        Log.d(TAG, "onPlayComplete $mp")
        next()
    }

    private fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        Log.e(TAG, "Playback Error: $what extra: $extra MediaPlayer: $mp")
        MediaPlayer.MEDIA_ERROR_IO
        mPlaybackListeners.forEach { it(nowPlaying()!!, STATE_STOP, -1) }
        updatePlaybackState(PlaybackStateCompat.STATE_ERROR)
        return true
    }

    private fun onBuffering(mediaPlayer: MediaPlayer, i: Int) {
        mPlaybackListeners.forEach { it(nowPlaying(), STATE_BUFFERING, i) }
        if (i < 100) return
        Log.d(TAG, "Fully buffered! $mediaPlayer")
        releaseWifiLock(mWifiLock)
    }

    private fun tryGainAudioFocus() {
        if (mHasFocus) {
            loseAudioFocus()
        }
        context {
            val am = it.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val req = AudioFocusRequest.Builder(AUDIOFOCUS_GAIN)
                        .setOnAudioFocusChangeListener(afChangeListener)
                        .setAcceptsDelayedFocusGain(false)
                        .setAudioAttributes(AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .build())
                        .build()
                am.requestAudioFocus(req)
            } else {
                // Request audio focus for playback
                am.requestAudioFocus(afChangeListener,
                        AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN)
            }

            mHasFocus =
                    if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        Log.w(TAG, "No AudioFocus Granted!")
                        false
                    } else {
                        true
                    }
        }
    }

    private fun loseAudioFocus() {
        context {
            val am = it.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val req = mAudioFocusRequest ?: return
                am.abandonAudioFocusRequest(req)
            } else {
                am.abandonAudioFocus(afChangeListener)
            }
        }
    }

    private fun onAudioFocusChange(focusChange: Int) {
        Log.d(TAG, "Focus Change!! " + focusChange)
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> pause()
            AUDIOFOCUS_LOSS_TRANSIENT -> pause()
            AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> player { it.setVolume(0.1f, 0.1f) }
            AudioManager.AUDIOFOCUS_GAIN -> player { it.setVolume(1f, 1f) }
        }
    }

    fun unregisterPlaybackListener(mPlaybackListener: (Song?, Int, Int) -> Unit) {
        mPlaybackListeners.remove(mPlaybackListener)
    }

    fun registerPlaybackListener(mPlaybackListener: (Song?, Int, Int) -> Unit) {
        mPlaybackListeners.add(mPlaybackListener)
    }

    private fun updatePlaybackState(state: Int) {
        val stateBuilder = PlaybackStateCompat.Builder().setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_STOP)

        stateBuilder.setState(state, queryPosMs(), 1f)
        mediaSession { it.setPlaybackState(stateBuilder.build()) }
    }

    private fun releaseBroadcastReceiver(context: Context,
                                         noiseAudioStreamReceiver: BroadcastReceiver?) {
        noiseAudioStreamReceiver ?: return
        try {
            context.unregisterReceiver(noiseAudioStreamReceiver)
        } catch (ex: IllegalStateException) {
            Log.w(TAG, "Can't release: " + noiseAudioStreamReceiver.javaClass.simpleName, ex)
        }
    }

    private fun acquireWifiLock(wifiLock: WifiManager.WifiLock?) {
        wifiLock ?: return
        if (!wifiLock.isHeld) {
            wifiLock.acquire()
        }
    }

    private fun releaseWifiLock(wifiLock: WifiManager.WifiLock?) {
        wifiLock ?: return
        if (wifiLock.isHeld) {
            wifiLock.release()
        }
    }

    private inline fun mediaSession(mediaSessionCallback: (MediaSessionCompat) -> Unit) {
        val mediaSession = mMediaSession
        if (mediaSession == null) {
            Log.w(TAG, "MediaSession: No MediaSession instance!!")
            return
        }
        mediaSessionCallback(mediaSession)
    }

    private inline fun player(playerCallback: (MediaPlayer) -> Unit) {
        val player = mPlayer
        if (player == null) {
            Log.w(TAG, "MediaPlayer: No Player instance!!")
            return
        }
        playerCallback(player)
    }

    private inline fun context(contextCallback: (Context) -> Unit) {
        val context = mContext
        if (context == null) {
            Log.w(TAG, "Context: No Context set!")
            return
        }
        contextCallback(context)
    }

    private inner class BecomingNoisyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent.action) {
                pause()
            }
        }
    }

    companion object {
        private val TAG = "HomefyPlayer"

        /*
         * Playback Codes
         */
        val STATE_PLAY = 0
        val STATE_PAUSE = 1
        val STATE_RESUME = 2
        val STATE_STOP = 3
        val STATE_BUFFERING = 4
    }
}
