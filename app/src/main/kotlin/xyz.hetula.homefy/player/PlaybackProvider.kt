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

import xyz.hetula.homefy.removeAtIfPresent
import java.util.*

internal interface PlaybackProvider {
    fun onPlayRequest(current: Song?, request: PlayRequest, nextInPlayback: ArrayList<Song>) {}
    fun moveToNext(current: Song?, nextInPlayback: ArrayList<Song>, originalRequest: PlayRequest): Song?
    fun moveToPrevious(current: Song?, previous: ArrayDeque<Song>, queueIt: () -> Unit): Song? {
        queueIt()
        return previous.pollLast() ?: current
    }
}

internal object NormalProvider : PlaybackProvider {

    override fun onPlayRequest(current: Song?, request: PlayRequest, nextInPlayback: ArrayList<Song>) {
        request.fill(nextInPlayback)
        rearrangeFromIndex(current, nextInPlayback)
    }

    override fun moveToNext(current: Song?, nextInPlayback: ArrayList<Song>, originalRequest: PlayRequest): Song? {
        return nextInPlayback.removeAtIfPresent(0)
    }
}

internal object RepeatProvider : PlaybackProvider {
    override fun onPlayRequest(current: Song?, request: PlayRequest, nextInPlayback: ArrayList<Song>) {
        request.fill(nextInPlayback)
        rearrangeFromIndex(current, nextInPlayback)
    }

    override fun moveToNext(current: Song?, nextInPlayback: ArrayList<Song>, originalRequest: PlayRequest): Song? {
        if (nextInPlayback.isEmpty()) {
            onPlayRequest(originalRequest.play, originalRequest, nextInPlayback)
            return originalRequest.play
        }
        return nextInPlayback.removeAt(0)
    }
}

internal object RandomProvider : PlaybackProvider {
    override fun moveToNext(current: Song?, nextInPlayback: ArrayList<Song>, originalRequest: PlayRequest): Song? {
        return originalRequest.getAny()
    }
}

internal object RepeatSingleProvider : PlaybackProvider {
    override fun moveToNext(current: Song?, nextInPlayback: ArrayList<Song>, originalRequest: PlayRequest): Song? {
        return current
    }

    override fun moveToPrevious(current: Song?, previous: ArrayDeque<Song>, queueIt: () -> Unit): Song? {
        return current
    }
}

private fun rearrangeFromIndex(current: Song?, nextInPlayback: ArrayList<Song>) {
    val indexInSongs = nextInPlayback.indexOf(current)
    if (indexInSongs <= 0) {
        nextInPlayback.removeAtIfPresent(0)
        return
    }
    val firstPart = nextInPlayback.subList(0, indexInSongs)
    val tmp = ArrayList(firstPart)
    firstPart.clear()
    nextInPlayback.removeAtIfPresent(0)
    nextInPlayback.addAll(tmp)
}
