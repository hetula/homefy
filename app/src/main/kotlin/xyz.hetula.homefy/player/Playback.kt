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

import android.util.Log
import java.util.*


/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
class Playback {
    private val previous = ArrayDeque<Song>()
    private val queue = ArrayList<Song>()
    private val next = ArrayList<Song>()

    private var playback: PlaybackProvider = RANDOM_PROVIDER
    private var lastRequest: PlayRequest? = null
    private var playing: Song? = null

    var playbackMode: PlaybackMode = PlaybackMode.RANDOM
        private set(value) {
            field = value
            setupPlaybackStyle(value)
        }

    fun stop() {
        playing?.clearAlbumArt()
        playing = null
    }

    fun playSong(play: Song, playlist: ArrayList<Song>) {
        playing?.clearAlbumArt()
        playing = play
        lastRequest = PlayRequest(play, playlist)

        next.clear()
        next.addAll(playlist)
        queue.clear()
    }

    private fun setupPlaybackStyle(mode: PlaybackMode) {
        playback = when (mode) {
            PlaybackMode.NORMAL -> NORMAL_PROVIDER
            PlaybackMode.REPEAT -> REPEAT_PROVIDER
            PlaybackMode.REPEAT_SINGLE -> REPEAT_SINGLE_PROVIDER
            PlaybackMode.RANDOM -> RANDOM_PROVIDER
        }
    }

    fun queueSong(queue: List<Song>) {
        this.queue.addAll(0, queue)
    }

    private fun queueSong(song: Song) {
        queue.add(0, song)
    }

    fun isEmpty(): Boolean {
        return playing == null
    }

    fun getCurrent(): Song? {
        return playing
    }

    inline fun getCurrent(songCallback: (Song) -> Unit) {
        val song = getCurrent() ?: return
        songCallback(song)
    }

    fun previous() {
        if (!isEmpty()) {
            queueSong(playing!!)
        }
        playing?.clearAlbumArt()
        playing = if (previous.isEmpty()) null else previous.removeLast()
    }

    fun next() {
        if (playing != null && playing != previous.peekLast()) {
            addToPrevious(playing!!)
        }
        playing?.clearAlbumArt()
        playing = if (!queue.isEmpty()) {
            queue.removeAt(0)
        } else {
            playback(playing, next, lastRequest)
        }
    }

    fun cyclePlaybackMode() {
        val ord = playbackMode.ordinal + 1
        playbackMode = if (ord >= PlaybackMode.values().size)
            PlaybackMode.values()[0]
        else
            PlaybackMode.values()[ord]

    }

    private fun addToPrevious(song: Song) {
        if (previous.size >= 100) {
            do { // Truncate
                Log.v("Playback", "Previous list is full => " + previous.size)
                previous.removeFirst()
            } while (previous.size >= 100)
        }
        previous.addLast(song)
    }

    private companion object {
        private val rnd = Random()
        val NORMAL_PROVIDER: PlaybackProvider = { _, nowPlaying, _ ->
            if (nowPlaying == null || nowPlaying.isEmpty()) null else nowPlaying.removeAt(0)
        }
        val REPEAT_PROVIDER: PlaybackProvider = { _, nowPlaying, lastRequest ->
            if (nowPlaying == null || lastRequest == null) null
            else {
                if (nowPlaying.isEmpty()) {
                    lastRequest.fill(nowPlaying)
                }
                nowPlaying.removeAt(0)
            }
        }
        val REPEAT_SINGLE_PROVIDER: PlaybackProvider = { now, _, _ -> now }
        val RANDOM_PROVIDER: PlaybackProvider = { _, _, lastRequest -> lastRequest?.getAny(rnd) }
    }
}

typealias PlaybackProvider = (Song?, ArrayList<Song>?, PlayRequest?) -> Song?
