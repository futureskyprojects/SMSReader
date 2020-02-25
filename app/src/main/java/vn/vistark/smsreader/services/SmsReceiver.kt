package vn.vistark.smsreader.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage
import android.widget.Toast
import vn.vistark.smsreader.data.db.MsgHandler
import vn.vistark.smsreader.model.Msg
import vn.vistark.smsreader.ui.setting_activity.SettingActivity
import vn.vistark.smsreader.utils.HttpPost
import vn.vistark.smsreader.utils.Vibrates
import java.lang.Exception

class SmsReceiver : BroadcastReceiver() {
    lateinit var prefs: SharedPreferences
    override fun onReceive(context: Context, intent: Intent) {
        prefs = context.getSharedPreferences(SettingActivity.PREFS_NAME, Context.MODE_PRIVATE)
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val smsMessages =
                    Telephony.Sms.Intents.getMessagesFromIntent(intent)
                smsProcessing(context, smsMessages)
            } else {
                val bundle = intent.extras
                if (bundle != null) {
                    val pdus = bundle.get("pdus") as Array<*>
                    val messages: Array<SmsMessage?> = arrayOfNulls(pdus.size)
                    for (i in pdus.indices) {
                        messages[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray)
                    }
                    if (messages.size > -1) {
                        smsProcessing(context, messages)
                    }
                }
            }
        }
    }

    private fun smsProcessing(context: Context, smsMessages: Array<SmsMessage?>) {
        val smsMessageArrayList = ArrayList<SmsMessage>()
        if (smsMessages.isNotEmpty()) {
            for (smsMessage in smsMessages) {
                if (smsMessage != null) {
                    smsMessageArrayList.add(smsMessage)
                }
            }
        }
        val froms = mutableMapOf<String, String>()
        for (smsMessage in smsMessageArrayList) {
            if (smsMessage.originatingAddress != null) {
                froms[smsMessage.originatingAddress!!] =
                    (froms[smsMessage.originatingAddress!!] ?: "") + smsMessage.messageBody
            }
        }

        for (from in froms) {
            msgProcessing(context, from.key, from.value)
        }
    }

    private fun msgProcessing(context: Context, number: String, message: String) {
        try {
            val apiAddress = prefs.getString(SettingActivity.API_ADDRESS, "")
            val token = prefs.getString(SettingActivity.SECURITY_CODE, "")
            val msg = Msg(System.currentTimeMillis(), number, message)
            // Tiến hành đẩy lên server
            if (HttpPost(context, msg.id).execute(
                    apiAddress,
                    msg.toJSONObject(token!!).toString()
                ).get()
            ) {
                Toast.makeText(
                    context,
                    "Đã gửi tin nhắn từ [$number] lên server",
                    Toast.LENGTH_SHORT
                ).show()
                // Nếu thành công, tiến hành rung để báo
                Vibrates.remindVibrate(context)
            } else {
                if (MsgHandler(context).add(msg) != (-1).toLong()) {
                    Toast.makeText(
                        context,
                        "Đã lưu tin nhắn từ thuê bao [$number] để xử lý sau",
                        Toast.LENGTH_SHORT
                    ).show()
                    Vibrates.remindVibrate(context)
                } else {
                    Toast.makeText(
                        context,
                        "Không xử lý được tin nhắn từ [$number], vui lòng thao tác bằng tay",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: Exception) {

        }
    }
}