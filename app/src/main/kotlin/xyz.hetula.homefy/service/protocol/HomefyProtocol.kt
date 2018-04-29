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
import com.android.volley.VolleyError

import xyz.hetula.homefy.player.Song

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
interface HomefyProtocol {

    var info: VersionInfo

    var server: String

    fun initialize(context: Context)

    fun release()

    fun setAuth(user: String, pass: String)

    fun addAuthHeader(headers: HashMap<String, String>)

    fun requestVersionInfo(versionConsumer: (VersionInfo) -> Unit, errorConsumer: (VolleyError) -> Unit)

    fun requestVersionInfoAuth(versionConsumer: (VersionInfo) -> Unit, errorConsumer: (VolleyError) -> Unit)

    fun requestSongs(songsConsumer: (Array<Song>) -> Unit, errorConsumer: (VolleyError) -> Unit)

    fun requestSongs(parameters: Map<String, String>?, songsConsumer: (Array<Song>) -> Unit, errorConsumer: (VolleyError) -> Unit)

    fun requestSong(id: String, songConsumer: (Song) -> Unit, errorConsumer: (VolleyError) -> Unit)

    fun requestPages(pageLength: Int, pagesConsumer: (Array<String>) -> Unit, errorConsumer: (VolleyError) -> Unit)

    fun <T> request(url: String, consumer: (T) -> Unit, errorConsumer: (VolleyError) -> Unit, clasz: Class<T>)

}
