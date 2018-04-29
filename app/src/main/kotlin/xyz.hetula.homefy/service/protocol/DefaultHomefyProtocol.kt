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
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import xyz.hetula.homefy.Utils
import xyz.hetula.homefy.player.Song
import java.nio.charset.StandardCharsets

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
class DefaultHomefyProtocol : HomefyProtocol {
    private var mQueryQueue: RequestQueue? = null
    private var mUserPass = ""
    private var mServerId = ""

    override var server: String = ""
        set(value) {
            field = value
            mServerId = Utils.getHash(value)
        }

    override var info = VersionInfo("", "Homefy", "0.0", "", VersionInfo.AuthType.NONE)

    override fun initialize(context: Context) {
        mQueryQueue = Volley.newRequestQueue(context.applicationContext)
    }

    override fun release() {
        mQueryQueue?.stop()
        mQueryQueue = null
    }

    override fun setAuth(user: String, pass: String) {
        mUserPass = String(Base64.encode("$user:$pass".toByteArray(StandardCharsets.UTF_8), 0),
                StandardCharsets.UTF_8)
    }

    override fun requestVersionInfo(versionConsumer: (VersionInfo) -> Unit,
                                    errorConsumer: (VolleyError) -> Unit) {
        val versionReq = GsonRequest(
                "$server/version",
                VersionInfo::class.java,
                { v ->
                    info = v
                    versionConsumer(v)
                },
                Response.ErrorListener({ error ->
                    Log.e(TAG, error.toString())
                    errorConsumer(error)
                }))
        mQueryQueue?.add(versionReq)
    }

    override fun requestVersionInfoAuth(versionConsumer: (VersionInfo) -> Unit,
                                        errorConsumer: (VolleyError) -> Unit) {
        val versionReq = GsonRequest(
                "$server/version/auth",
                VersionInfo::class.java,
                { v ->
                    info = v
                    versionConsumer(v)
                },
                Response.ErrorListener({ error ->
                    Log.e(TAG, error.toString())
                    errorConsumer(error)
                }))
        appendHeaders(versionReq)
        mQueryQueue?.add(versionReq)
    }

    override fun requestPages(pageLength: Int,
                              pagesConsumer: (Array<String>) -> Unit,
                              errorConsumer: (VolleyError) -> Unit) {
        val pagesRequest = GsonRequest(
                "$server/songs/pages?length=$pageLength",
                Array<String>::class.java,
                pagesConsumer,
                Response.ErrorListener({ error ->
                    Log.e(TAG, error.toString())
                    errorConsumer(error)
                }))
        appendHeaders(pagesRequest)
        mQueryQueue?.add(pagesRequest)
    }

    override fun requestSongs(songsConsumer: (Array<Song>) -> Unit,
                              errorConsumer: (VolleyError) -> Unit) {
        requestSongs(null, songsConsumer, errorConsumer)
    }

    override fun requestSongs(parameters: Map<String, String>?,
                              songsConsumer: (Array<Song>) -> Unit,
                              errorConsumer: (VolleyError) -> Unit) {
        val params = StringBuilder()
        if (parameters != null && parameters.isEmpty()) {
            params.append('?')
            for (key in parameters.keys) {
                params.append(key)
                        .append('=')
                        .append(parameters[key])
                        .append('&')
            }
            params.deleteCharAt(params.length - 1)
        }
        val songsReq = GsonRequest(
                "$server/songs" + params.toString(),
                Array<Song>::class.java,
                songsConsumer,
                Response.ErrorListener({ error ->
                    Log.e(TAG, error.toString())
                    errorConsumer(error)
                }))
        appendHeaders(songsReq)
        mQueryQueue?.add(songsReq)
    }

    override fun requestSong(id: String,
                             songConsumer: (Song) -> Unit,
                             errorConsumer: (VolleyError) -> Unit) {
        val songReq = GsonRequest(
                "$server/song/$id",
                Song::class.java,
                songConsumer,
                Response.ErrorListener({ error ->
                    Log.e(TAG, error.toString())
                    errorConsumer(error)
                }))
        appendHeaders(songReq)
        mQueryQueue?.add(songReq)
    }

    override fun <T> request(url: String,
                             consumer: (T) -> Unit,
                             errorConsumer: (VolleyError) -> Unit,
                             clasz: Class<T>) {
        val request = GsonRequest(
                url,
                clasz,
                consumer,
                Response.ErrorListener({ error ->
                    Log.e(TAG, error.toString())
                    errorConsumer(error)
                }))
        appendHeaders(request)
        mQueryQueue?.add(request)
    }

    override fun addAuthHeader(headers: HashMap<String, String>) {
        if (TextUtils.isEmpty(mUserPass)) return
        headers["Authorization"] = "Basic $mUserPass"
    }

    private fun appendHeaders(request: GsonRequest<*>) {
        if (TextUtils.isEmpty(mUserPass)) return
        request.putHeader("Authorization", "Basic $mUserPass")
    }

    companion object {
        private const val TAG = "HomefyProtocol"
    }
}
