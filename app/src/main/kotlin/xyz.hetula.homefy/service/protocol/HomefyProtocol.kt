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
