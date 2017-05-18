package xyz.hetula.homefy.service.protocol

import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.UnsupportedEncodingException
import java.util.*

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
*/
class GsonRequest<T> : Request<T> {
    private val gson = Gson()
    private val clazz: Class<T>
    private val headers: MutableMap<String, String>?
    private val listener: (T) -> Unit

    /**
     * Make a GET request and return a parsed object from JSON.

     * @param url URL of the request to make
     * *
     * @param clazz Relevant class object, for Gson's reflection
     * *
     * @param headers Map of request headers
     */
    constructor(url: String, clazz: Class<T>, headers: MutableMap<String, String>,
                listener: (T) -> Unit, errorListener: Response.ErrorListener) :
            super(Request.Method.GET, url, errorListener) {
        this.clazz = clazz
        this.headers = headers
        this.listener = listener
    }

    /**
     * Make a GET request and return a parsed object from JSON.

     * @param url URL of the request to make
     * *
     * @param clazz Relevant class object, for Gson's reflection
     */
    constructor(url: String, clazz: Class<T>, listener: (T) -> Unit,
                errorListener: Response.ErrorListener) :
            super(Request.Method.GET, url, errorListener) {
        this.clazz = clazz
        this.headers = HashMap<String, String>()
        this.listener = listener
    }

    @Throws(AuthFailureError::class)
    override fun getHeaders(): Map<String, String> {
        return headers ?: super.getHeaders()
    }

    internal fun putHeader(header: String, value: String) {
        headers!!.put(header, value)
    }

    override fun deliverResponse(response: T) {
        listener(response)
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<T> {
        try {
            val json = String(
                    response.data,
                    charset(HttpHeaderParser.parseCharset(response.headers)))
            return Response.success(
                    gson.fromJson(json, clazz),
                    HttpHeaderParser.parseCacheHeaders(response))
        } catch (e: UnsupportedEncodingException) {
            return Response.error<T>(ParseError(e))
        } catch (e: JsonSyntaxException) {
            return Response.error<T>(ParseError(e))
        }

    }
}
