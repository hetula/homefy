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

package xyz.hetula.homefy

import android.content.Intent
import xyz.hetula.homefy.player.Song
import kotlin.test.assertEquals

object TestHelper {
    fun makeSongList(count: Int): ArrayList<Song> {
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

    fun compareIntents(expected: Intent, actual: Intent) {
        assertEquals(expected.action, actual.action, "Actions differ")
        assertEquals(expected.component, actual.component)
        val expectedHasExtras = expected.extras != null
        val actualHasExtras = actual.extras != null
        assertEquals(expectedHasExtras, actualHasExtras)
        if (expectedHasExtras) {
            assertEquals(expected.extras!!.size(), actual.extras!!.size())
        }
        // TODO: More specific checks
    }
}
