/*
 * Copyright (c) 2018 Tuomo Heino
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.hetula.homefy.player

import android.support.annotation.VisibleForTesting
import android.util.Log
import java.util.*


/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
class Playback {
    private val mPrevious = ArrayDeque<Song>()
    private val mQueue = ArrayList<Song>()
    private val mNext = ArrayList<Song>()

    private var mRnd = Random()
    private var mPlaybackProvider: PlaybackProvider = RandomProvider
    private var mLastPlayRequest: PlayRequest? = null
    private var mNowPlaying: Song? = null

    var playbackMode: PlaybackMode = PlaybackMode.RANDOM
        @VisibleForTesting
        internal set(value) {
            field = value
            setupPlaybackStyle(value)
        }

    fun playSong(play: Song, playlist: ArrayList<Song>) {
        mNowPlaying = play
        val newPlayRequest = PlayRequest(play, playlist, mRnd)

        mNext.clear()
        mQueue.clear()
        mPlaybackProvider.onPlayRequest(play, newPlayRequest, mNext)
        mLastPlayRequest = newPlayRequest
    }

    fun queueSong(queue: List<Song>) {
        this.mQueue.addAll(0, queue)
    }

    fun next() {
        val lastPlayRequest = mLastPlayRequest ?: return
        val current = mNowPlaying
        if (current != null && current != mPrevious.peekLast()) {
            addToPrevious(current)
        }
        mNowPlaying = if (mQueue.isNotEmpty()) {
            mQueue.removeAt(0)
        } else {
            mPlaybackProvider.moveToNext(mNowPlaying, mNext, lastPlayRequest)
        }
    }

    fun previous() {
        val nowPlaying = mNowPlaying
        mNowPlaying = mPlaybackProvider.moveToPrevious(nowPlaying, mPrevious) {
            if (nowPlaying != null) {
                queueSong(nowPlaying)
            }
        }
    }

    fun stop() {
        mNowPlaying = null
    }

    fun hasPlayback(): Boolean {
        return mNowPlaying == null
    }

    fun getCurrent(): Song? {
        return mNowPlaying
    }

    inline fun getCurrent(songCallback: (Song) -> Unit) {
        val song = getCurrent() ?: return
        songCallback(song)
    }

    fun cyclePlaybackMode() {
        val ord = playbackMode.ordinal + 1
        playbackMode = if (ord >= PlaybackMode.values().size)
            PlaybackMode.values()[0]
        else
            PlaybackMode.values()[ord]

        val lastPlaybackRequest = mLastPlayRequest ?: return
        mNext.clear()
        mPlaybackProvider.onPlayRequest(mNowPlaying, lastPlaybackRequest, mNext)
    }

    private fun queueSong(song: Song) {
        if (mQueue.isNotEmpty() && mQueue[0] == song) {
            Log.i("Playback", "Queue already contains given song as first, not adding again!")
        } else {
            mQueue.add(0, song)
        }
    }

    private fun setupPlaybackStyle(mode: PlaybackMode) {
        mPlaybackProvider = when (mode) {
            PlaybackMode.NORMAL -> NormalProvider
            PlaybackMode.REPEAT -> RepeatProvider
            PlaybackMode.REPEAT_SINGLE -> RepeatSingleProvider
            PlaybackMode.RANDOM -> RandomProvider
        }
    }

    private fun addToPrevious(song: Song) {
        if (mPrevious.size >= 100) {
            do { // Truncate
                Log.v("Playback", "Previous list is full => " + mPrevious.size)
                mPrevious.removeFirst()
            } while (mPrevious.size >= 100)
        }
        mPrevious.addLast(song)
    }

    @VisibleForTesting
    internal fun getPrevious(): ArrayDeque<Song> {
        return mPrevious
    }

    @VisibleForTesting
    internal fun getNext(): List<Song> {
        return mNext
    }

    @VisibleForTesting
    internal fun setupRandom(i: Long) {
        mRnd = Random(i)
    }
}
