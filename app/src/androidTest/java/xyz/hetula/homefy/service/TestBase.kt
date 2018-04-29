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

package xyz.hetula.homefy.service

import android.content.Context
import android.os.SystemClock
import android.support.test.InstrumentationRegistry
import android.util.Log
import xyz.hetula.homefy.library.HomefyLibrary
import xyz.hetula.homefy.player.TestHomefyPlayer
import xyz.hetula.homefy.playlist.TestHomefyPlaylist
import xyz.hetula.homefy.service.protocol.HomefyProtocol
import xyz.hetula.homefy.service.protocol.TestProtocol

abstract class TestBase {
    val context: Context by lazy { InstrumentationRegistry.getTargetContext() }
    lateinit var protocol: TestProtocol
    lateinit var player: TestHomefyPlayer
    lateinit var playlist: TestHomefyPlaylist


    fun createServices() {
        Log.d("TestBase", "Overriding Initializers!")
        ServiceInitializer.protocol = {
            protocol = TestProtocol()
            protocol
        }
        ServiceInitializer.player = { protocol: HomefyProtocol, library: HomefyLibrary ->
            player = TestHomefyPlayer(protocol, library)
            player
        }
        ServiceInitializer.playlist = {
            playlist = TestHomefyPlaylist()
            playlist
        }
    }

    fun waitSome(ms: Long) {
        SystemClock.sleep(ms)
    }
}
