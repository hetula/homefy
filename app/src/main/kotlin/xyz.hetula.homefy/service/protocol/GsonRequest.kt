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

import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import java.io.UnsupportedEncodingException
import java.util.*

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
class GsonRequest<T>(url: String,
                     private val clazz: Class<T>,
                     private val listener: (T) -> Unit,
                     errorListener: Response.ErrorListener) :
        Request<T>(Request.Method.GET, url, errorListener) {

    private val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
    private val headers = HashMap<String, String>()

    @Throws(AuthFailureError::class)
    override fun getHeaders(): Map<String, String> {
        return headers
    }

    internal fun putHeader(header: String, value: String) {
        headers[header] = value
    }

    override fun deliverResponse(response: T) {
        listener(response)
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<T> {
        return try {
            val json = String(
                    response.data,
                    charset(HttpHeaderParser.parseCharset(response.headers)))
            Response.success(
                    gson.fromJson(json, clazz),
                    HttpHeaderParser.parseCacheHeaders(response))
        } catch (e: UnsupportedEncodingException) {
            Response.error<T>(ParseError(e))
        } catch (e: JsonSyntaxException) {
            Response.error<T>(ParseError(e))
        }

    }
}
