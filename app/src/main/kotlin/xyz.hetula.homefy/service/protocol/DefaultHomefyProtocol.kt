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
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import xyz.hetula.homefy.player.Song
import xyz.hetula.homefy.toSHA1Hash
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
            mServerId = value.toSHA1Hash()
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
                                    errorConsumer: (RequestError) -> Unit) {
        val versionReq = GsonRequest(
                "$server/version",
                VersionInfo::class.java,
                { v ->
                    info = v
                    versionConsumer(v)
                },
                Response.ErrorListener({ error ->
                    Log.e(TAG, error.toString())
                    val code = getErrorCode(error)
                    errorConsumer(RequestError(code, error))
                }))
        mQueryQueue?.add(versionReq)
    }

    override fun requestVersionInfoAuth(versionConsumer: (VersionInfo) -> Unit,
                                        errorConsumer: (RequestError) -> Unit) {
        val versionReq = GsonRequest(
                "$server/version/auth",
                VersionInfo::class.java,
                { v ->
                    info = v
                    versionConsumer(v)
                },
                Response.ErrorListener({ error ->
                    Log.e(TAG, error.toString())
                    val code = getErrorCode(error)
                    errorConsumer(RequestError(code, error))
                }))
        appendHeaders(versionReq)
        mQueryQueue?.add(versionReq)
    }

    override fun requestPages(pageLength: Int,
                              pagesConsumer: (Array<String>) -> Unit,
                              errorConsumer: (RequestError) -> Unit) {
        val pagesRequest = GsonRequest(
                "$server/songs/pages?length=$pageLength",
                Array<String>::class.java,
                pagesConsumer,
                Response.ErrorListener({ error ->
                    Log.e(TAG, error.toString())
                    val code = getErrorCode(error)
                    errorConsumer(RequestError(code, error))
                }))
        appendHeaders(pagesRequest)
        mQueryQueue?.add(pagesRequest)
    }

    override fun requestSongs(songsConsumer: (Array<Song>) -> Unit,
                              errorConsumer: (RequestError) -> Unit) {
        requestSongs(null, songsConsumer, errorConsumer)
    }

    override fun requestSongs(parameters: Map<String, String>?,
                              songsConsumer: (Array<Song>) -> Unit,
                              errorConsumer: (RequestError) -> Unit) {
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
                    val code = getErrorCode(error)
                    errorConsumer(RequestError(code, error))
                }))
        appendHeaders(songsReq)
        mQueryQueue?.add(songsReq)
    }

    override fun requestSong(id: String,
                             songConsumer: (Song) -> Unit,
                             errorConsumer: (RequestError) -> Unit) {
        val songReq = GsonRequest(
                "$server/song/$id",
                Song::class.java,
                songConsumer,
                Response.ErrorListener({ error ->
                    Log.e(TAG, error.toString())
                    val code = getErrorCode(error)
                    errorConsumer(RequestError(code, error))
                }))
        appendHeaders(songReq)
        mQueryQueue?.add(songReq)
    }

    override fun <T> request(url: String,
                             consumer: (T) -> Unit,
                             errorConsumer: (RequestError) -> Unit,
                             clasz: Class<T>) {
        val request = GsonRequest(
                url,
                clasz,
                consumer,
                Response.ErrorListener({ error ->
                    Log.e(TAG, error.toString())
                    val code = getErrorCode(error)
                    errorConsumer(RequestError(code, error))
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

    private fun getErrorCode(volleyError: VolleyError?): Int {
        volleyError ?: return -1
        volleyError.networkResponse ?: return -1
        return volleyError.networkResponse.statusCode
    }

    companion object {
        private const val TAG = "HomefyProtocol"
    }
}
