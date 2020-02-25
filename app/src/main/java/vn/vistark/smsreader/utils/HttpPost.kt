package vn.vistark.smsreader.utils

import android.content.Context
import android.os.AsyncTask
import android.os.Build
import org.json.JSONObject
import vn.vistark.smsreader.data.db.MsgHandler
import java.io.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection

class HttpPost(val context: Context, val msgId: Long) : AsyncTask<String, Void, Boolean>() {
    var apiAddress = ""
    var data = ""
    override fun doInBackground(vararg params: String?): Boolean {
        apiAddress = params[0]!!
        data = params[1]!!
//        println(apiAddress)
//        println(data)
        try {
            val url = URL(apiAddress)
            val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8")
            conn.setRequestProperty("Accept", "application/json")
            conn.doOutput = true
            conn.doInput = true
            conn.readTimeout = 10000
            val os = DataOutputStream(conn.outputStream)
            val writer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                BufferedWriter(
                    OutputStreamWriter(os, StandardCharsets.UTF_8)
                )
            } else {
                BufferedWriter(
                    OutputStreamWriter(os, "UTF-8")
                )
            }
            writer.write(data)
            writer.flush()
            writer.close()
            os.close()
            if (conn.responseCode == HttpsURLConnection.HTTP_OK) {
//                conn.inputStream.bufferedReader().use {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        it.lines().forEach { line ->
//                            println(line)
//                        }
//                    }
//                }
                conn.disconnect()
                // Gửi thành công
                return true
            } else {
                conn.disconnect()
                // Gửi không thành công
                return false
            }
        } catch (e: Exception) {
            return false
        }
    }

    override fun onPostExecute(result: Boolean) {
        if (result) {
            MsgHandler(context).remove(msgId)
        }
        super.onPostExecute(result)
    }

}