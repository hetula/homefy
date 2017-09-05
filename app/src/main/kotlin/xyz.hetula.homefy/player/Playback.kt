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
        get
        private set(value) {
            field = value
            setupPlaybackStyle(value)
        }

    fun stop() {
        playing = null
    }

    fun playSong(play: Song, playlist: ArrayList<Song>) {
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

    fun previous() {
        if (!isEmpty()) {
            queueSong(playing!!)
        }
        playing = if (previous.isEmpty()) null else previous.removeLast()
    }

    fun next() {
        if (playing != null && playing != previous.peekLast()) {
            addToPrevious(playing!!)
        }
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
