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

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import xyz.hetula.homefy.useFor
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(JUnit4::class)
class PlaybackTest {

    @Test
    fun repeatSinglePlayback() {
        val playback = Playback()
        playback.playbackMode = PlaybackMode.REPEAT_SINGLE
        assertEquals(PlaybackMode.REPEAT_SINGLE, playback.playbackMode)
        val songs = makeSongList(2)
        playback.playSong(songs[1], songs)
        assertEquals(0, playback.getNext().size)
        assertEquals(songs[1], playback.getCurrent())
        playback.next()
        assertEquals(songs[1], playback.getCurrent())
        assertEquals(1, playback.getPrevious().size)
        playback.next()
        assertEquals(songs[1], playback.getCurrent())
        assertEquals(1, playback.getPrevious().size)
        playback.next()
        assertEquals(songs[1], playback.getCurrent())
        assertEquals(1, playback.getPrevious().size)
    }

    @Test
    fun randomPlaybackMode() {
        val playback = Playback()
        playback.setupRandom(2) // DO NOT TOUCH! Whole test is using this as an basis
        assertEquals(PlaybackMode.RANDOM, playback.playbackMode)
        assertNull(playback.getCurrent())
        val songs = makeSongList(10)
        playback.playSong(songs[0], ArrayList(songs))
        assertEquals(songs[0], playback.getCurrent())
        assertEquals(0, playback.getPrevious().size)
        playback.next()
        assertEquals(songs[1], playback.getCurrent())
        assertEquals(1, playback.getPrevious().size)
        assertEquals(songs[0], playback.getPrevious().last)
        playback.next()
        assertEquals(songs[2], playback.getCurrent())
        assertEquals(2, playback.getPrevious().size)
        assertEquals(songs[1], playback.getPrevious().last)
        playback.next()
        assertEquals(songs[9], playback.getCurrent())
        assertEquals(3, playback.getPrevious().size)
        assertEquals(songs[2], playback.getPrevious().last)
    }

    @Test
    fun normalPlayback() {
        val playback = Playback()
        playback.playbackMode = PlaybackMode.NORMAL
        assertEquals(PlaybackMode.NORMAL, playback.playbackMode)
        val songs = makeSongList(10)
        playback.playSong(songs[0], ArrayList(songs))
        assertEquals(songs[0], playback.getCurrent())
        assertEquals(0, playback.getPrevious().size)
        assertEquals(9, playback.getNext().size)
        playback.next()
        assertEquals(songs[1], playback.getCurrent())
        assertEquals(1, playback.getPrevious().size)
        assertEquals(songs[0], playback.getPrevious().last)
        assertEquals(8, playback.getNext().size)
        playback.next()
        assertEquals(songs[2], playback.getCurrent())
        assertEquals(2, playback.getPrevious().size)
        assertEquals(songs[1], playback.getPrevious().last)
        assertEquals(7, playback.getNext().size)
        playback.next()
        assertEquals(songs[3], playback.getCurrent())
        assertEquals(3, playback.getPrevious().size)
        assertEquals(songs[2], playback.getPrevious().last)
        assertEquals(6, playback.getNext().size)
        playback.previous()
        assertEquals(songs[2], playback.getCurrent())
        assertEquals(2, playback.getPrevious().size)
        assertEquals(songs[1], playback.getPrevious().last)
        assertEquals(6, playback.getNext().size)
    }

    @Test
    fun repeatPlayback() {
        val playback = Playback()
        playback.playbackMode = PlaybackMode.REPEAT
        assertEquals(PlaybackMode.REPEAT, playback.playbackMode)
        val songs = makeSongList(3)
        playback.playSong(songs[0], ArrayList(songs))
        assertEquals(songs[0], playback.getCurrent())
        assertEquals(2, playback.getNext().size)
        playback.next()
        assertEquals(songs[1], playback.getCurrent())
        assertEquals(1, playback.getPrevious().size)
        assertEquals(songs[0], playback.getPrevious().last)
        assertEquals(1, playback.getNext().size)
        playback.next()
        assertEquals(songs[2], playback.getCurrent())
        assertEquals(2, playback.getPrevious().size)
        assertEquals(songs[1], playback.getPrevious().last)
        assertEquals(0, playback.getNext().size)
        playback.next()
        assertEquals(songs[0], playback.getCurrent())
        assertEquals(3, playback.getPrevious().size)
        assertEquals(songs[2], playback.getPrevious().last)
        assertEquals(2, playback.getNext().size)
    }

    private fun makeSongList(count: Int): ArrayList<Song> {
        val songs = ArrayList<Song>()
        count.useFor {
            songs.add(Song(
                    id = "$it",
                    title = "title_$it",
                    artist = "artist_$it",
                    album = "album_$it",
                    length = it * 1000L
            ))
        }
        assertEquals(count, songs.size)
        return songs
    }
}
