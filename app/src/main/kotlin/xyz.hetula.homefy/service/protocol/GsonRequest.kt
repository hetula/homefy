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
class GsonRequest<T>(url: String,
                     private val clazz: Class<T>,
                     private val listener: (T) -> Unit,
                     errorListener: Response.ErrorListener) :
        Request<T>(Request.Method.GET, url, errorListener) {

    private val gson = Gson()
    private val headers = HashMap<String, String>()

    @Throws(AuthFailureError::class)
    override fun getHeaders(): Map<String, String> {
        return headers
    }

    internal fun putHeader(header: String, value: String) {
        headers.put(header, value)
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
