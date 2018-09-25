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

package xyz.hetula.homefy.library

import android.content.Context
import android.content.Intent
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import xyz.hetula.homefy.TestCutor
import xyz.hetula.homefy.TestHelper.compareIntents
import xyz.hetula.homefy.TestHelper.makeSongList
import xyz.hetula.homefy.player.Song
import xyz.hetula.homefy.service.HomefyService
import xyz.hetula.homefy.service.protocol.HomefyProtocol
import java.util.*
import kotlin.collections.ArrayList
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class LibraryTest {
    private lateinit var mockProtocol: HomefyProtocol
    private lateinit var mockContext: Context

    private lateinit var library: HomefyLibrary

    private var nextIntent: Intent = Intent()

    @Before
    fun setup() {
        mockProtocol = mock(HomefyProtocol::class.java)
        mockContext = mock(Context::class.java)
        `when`(mockContext.packageName).thenReturn("test.package")
        `when`(mockContext.startService(any(Intent::class.java))).then {
            val intent = it.getArgument<Intent>(0)
            compareIntents(nextIntent, intent)
            intent.component
        }
        library = HomefyLibrary(mockProtocol, TestCutor())
    }

    @After
    fun cleanUp() {
        verifyNoMoreInteractions(mockProtocol)
    }

    @Test
    fun libraryInit() {
        assertEquals(1, 1)
        val songsToAdd = makeSongList(10)
        val shuffled = ArrayList(songsToAdd)
        shuffled.shuffle(Random(1))


        nextIntent = Intent(mockContext, HomefyService::class.java)
        nextIntent.action = HomefyService.INIT_COMPLETE

        library.initialize(mockContext, shuffled)

        verifySongsInOrder(songsToAdd, library)
        verifyAllSongsPresent(songsToAdd, library)
    }

    private fun verifySongsInOrder(correctOrder: List<Song>, library: HomefyLibrary) {
        assertEquals(correctOrder.size, library.songs.size)
        for (i in 0 until correctOrder.size) {
            assertEquals(correctOrder[i], library.songs[i])
        }
    }

    private fun verifyAllSongsPresent(allSongs: List<Song>, library: HomefyLibrary) {
        for (song in allSongs) {
            assertEquals(song, library.getSongById(song.id))
        }
    }
}
