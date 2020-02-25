package vn.vistark.smsreader.model

import org.json.JSONObject

class Msg(var id: Long, var phone: String, var msg: String) {
    companion object {
        const val TABLE_NAME = "MSG_TABLE"
        const val COL_ID = "COL_ID"
        const val COL_PHONE = "COL_PHONE"
        const val COL_MSG = "COL_MSG"
    }

    fun toJSONObject(token: String = ""): JSONObject {
        val body = JSONObject()
        body.put("sender", phone)
        body.put("message", msg)
        body.put("timestamp", id)
        body.put("token", token)
        return body
    }
}