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

package xyz.hetula.homefy.service.protocol

import android.content.Context
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import com.android.volley.VolleyError
import xyz.hetula.homefy.Utils
import xyz.hetula.homefy.player.Song
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class TestProtocol : HomefyProtocol {
    init {
        Log.d("TestProtocol", "Created TestProtocol")
    }

    lateinit var requestVersionInfo: (String, (VersionInfo) -> Unit) -> Unit
    lateinit var requestPages: (pageLength: Int, pagesConsumer: (Array<String>) -> Unit) -> Unit


    private val executor = Executors.newSingleThreadExecutor()
    private val tag = "TestProtocol"
    private var mUserPass = ""
    private var mServerId = ""

    override var info = VersionInfo("", "Homefy", "0.0", "", VersionInfo.AuthType.NONE)

    override var server: String = ""
        set(value) {
            field = value
            mServerId = Utils.getHash(value)
        }

    override fun initialize(context: Context) {
        Log.d(tag, "Initialize!")
    }

    override fun release() {
        Log.d(tag, "Release!")
        executor.shutdown()
        executor.awaitTermination(2, TimeUnit.SECONDS)
        executor.shutdownNow()
    }

    override fun setAuth(user: String, pass: String) {
        mUserPass = String(Base64.encode((user + ":" + pass)
                .toByteArray(StandardCharsets.UTF_8), 0), StandardCharsets.UTF_8)
    }

    override fun addAuthHeader(headers: HashMap<String, String>) {
        if (TextUtils.isEmpty(mUserPass)) return
        headers.put("Authorization", "Basic " + mUserPass)
    }

    override fun requestVersionInfo(versionConsumer: (VersionInfo) -> Unit, errorConsumer: (VolleyError) -> Unit) {
        executor.submit { requestVersionInfo("$server/version", versionConsumer) }
    }

    override fun requestVersionInfoAuth(versionConsumer: (VersionInfo) -> Unit, errorConsumer: (VolleyError) -> Unit) {
    }

    override fun requestSongs(songsConsumer: (Array<Song>) -> Unit, errorConsumer: (VolleyError) -> Unit) {
    }

    override fun requestSongs(parameters: Map<String, String>?, songsConsumer: (Array<Song>) -> Unit, errorConsumer: (VolleyError) -> Unit) {
    }

    override fun requestSong(id: String, songConsumer: (Song) -> Unit, errorConsumer: (VolleyError) -> Unit) {
    }

    override fun requestPages(pageLength: Int, pagesConsumer: (Array<String>) -> Unit, errorConsumer: (VolleyError) -> Unit) {
        executor.submit { requestPages(pageLength, pagesConsumer) }
    }

    override fun <T> request(url: String, consumer: (T) -> Unit, errorConsumer: (VolleyError) -> Unit, clasz: Class<T>) {

    }

}
