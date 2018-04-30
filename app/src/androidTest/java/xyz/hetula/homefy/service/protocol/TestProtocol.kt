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

package xyz.hetula.homefy.service.protocol

import android.content.Context
import android.text.TextUtils
import android.util.Base64
import android.util.Log
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
        mUserPass = String(Base64.encode(("$user:$pass")
                .toByteArray(StandardCharsets.UTF_8), 0), StandardCharsets.UTF_8)
    }

    override fun addAuthHeader(headers: HashMap<String, String>) {
        if (TextUtils.isEmpty(mUserPass)) return
        headers["Authorization"] = "Basic $mUserPass"
    }

    override fun requestVersionInfo(versionConsumer: (VersionInfo) -> Unit, errorConsumer: (RequestError) -> Unit) {
        executor.submit { requestVersionInfo("$server/version", versionConsumer) }
    }

    override fun requestVersionInfoAuth(versionConsumer: (VersionInfo) -> Unit, errorConsumer: (RequestError) -> Unit) {
    }

    override fun requestSongs(songsConsumer: (Array<Song>) -> Unit, errorConsumer: (RequestError) -> Unit) {
    }

    override fun requestSongs(parameters: Map<String, String>?, songsConsumer: (Array<Song>) -> Unit, errorConsumer: (RequestError) -> Unit) {
    }

    override fun requestSong(id: String, songConsumer: (Song) -> Unit, errorConsumer: (RequestError) -> Unit) {
    }

    override fun requestPages(pageLength: Int, pagesConsumer: (Array<String>) -> Unit, errorConsumer: (RequestError) -> Unit) {
        executor.submit { requestPages(pageLength, pagesConsumer) }
    }

    override fun <T> request(url: String, consumer: (T) -> Unit, errorConsumer: (RequestError) -> Unit, clasz: Class<T>) {

    }

}
