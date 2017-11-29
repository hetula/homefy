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
