package vn.vistark.smsreader.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.widget.Toast
import vn.vistark.smsreader.data.db.MsgHandler
import vn.vistark.smsreader.model.Msg
import vn.vistark.smsreader.ui.setting_activity.SettingActivity
import java.io.BufferedWriter
import java.io.DataOutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection

class HttpPost(val context: Context, val msg: Msg) : AsyncTask<String, Void, Boolean>() {
    var apiAddress = SettingActivity.DEFAULT_API
    var data = ""
    @SuppressLint("DefaultLocale")
    override fun doInBackground(vararg params: String?): Boolean {
        if (!params[0]!!.isEmpty())
            apiAddress = params[0]!!
        data = msg.msg

        val reqParam = URLEncoder.encode("data", "UTF-8") + "=" + URLEncoder.encode(data, "UTF-8")
//        println(apiAddress)
//        println(data)
        println(reqParam)
        try {
            val url = URL(apiAddress)
            val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Accept-Charset", "UTF-8");
//            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8")
//            conn.setRequestProperty("Accept", "application/json")
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
            writer.write(reqParam)
            writer.flush()
            writer.close()
            os.close()
            if (conn.responseCode == HttpsURLConnection.HTTP_OK) {
                var res = StringBuffer()
                conn.inputStream.bufferedReader().use {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        it.lines().forEach { line ->
                            res.append(line)
                        }
                    }
                }
                conn.disconnect()
                println("$res <<<<<<<<<<<<<<<<<<<<<<")
                return (res.toString().toLowerCase().contains("Th\\u00e0nh c\\u00f4ng".toLowerCase()) ||
                        res.toString().toLowerCase().contains("c\\u00f3 l\\u1ed7i".toLowerCase()))
            } else {
                conn.disconnect()
                // Gửi không thành công
                println(">>>><><> GỬI KHÔNG THÀNH CÔNG")
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override fun onPostExecute(result: Boolean) {
        val msgHandler = MsgHandler(context)
        val msgs = msgHandler.getAll()
        var isHave = false
        for (m in msgs) {
            println(m.id.toString() + "| ${msg.id} >>>>>>>>>>>>>>>>>")
            if (m.id == msg.id) {
                isHave = true
            }
        }
        println("Hoàn tất gửi mẫu tin mã số ${msg.id} với kết quả là ${result.toString()} - ${isHave.toString()} đến ${apiAddress}")
        if (result) {
            if (msg.id != (-1).toLong() && isHave) {
                msgHandler.remove(msg.id)
            }
        } else if (!isHave) {
            // Lưu lại tin vào csdl nếu chưa có
            if (msgHandler.add(msg) != (-1).toLong()) {
                Toast.makeText(
                    context,
                    "Đã lưu tin nhắn từ thuê bao [${msg.phone}] để xử lý sau",
                    Toast.LENGTH_SHORT
                ).show()
                Vibrates.remindVibrate(context)
            } else {
                Toast.makeText(
                    context,
                    "Không xử lý được tin nhắn từ [${msg.phone}], vui lòng thao tác bằng tay",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        super.onPostExecute(result)
    }

    fun decodeUnicode(inp: String): String {
        val str = inp.replace("\\", "")
        val arr = str.split("u")
        var text = ""
        for (i in 1 until arr.size) {
            val hexVal = arr[i].toInt(16)
            text += hexVal.toChar()
        }
        return text
    }
}