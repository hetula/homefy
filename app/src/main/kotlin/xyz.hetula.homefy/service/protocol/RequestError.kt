package xyz.hetula.homefy.service.protocol

import com.android.volley.VolleyError

data class RequestError(val errCode: Int, val volleyError: VolleyError)
