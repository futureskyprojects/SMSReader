package vn.vistark.smsreader.services

import android.annotation.SuppressLint
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

    @SuppressLint("DefaultLocale")
    private fun msgProcessing(context: Context, number: String, message: String) {
        try {
            val apiAddress = prefs.getString(SettingActivity.API_ADDRESS, "")
            val phoneFilter = prefs.getString(SettingActivity.PHONE_FILTER, "")
            var isMatched = false
            if (phoneFilter != null && !phoneFilter.isEmpty() && phoneFilter.isNotEmpty()) {
                val list = phoneFilter.split(",")
                for (p in list) {
                    if ((p.toLowerCase().contains(number.toLowerCase()) ||
                                number.toLowerCase().contains(p.toLowerCase())) && p.isNotBlank() && number.isNotBlank()
                    ) {
                        isMatched = true
                    }
                }
            } else {
                isMatched = true
            }
            if (isMatched) {
                val msg = Msg(System.currentTimeMillis(), number, message)
                // Tiến hành đẩy lên server
                Vibrates.remindVibrate(context)
                // xử lý và đẩy tin lên server
                HttpPost(context, msg).execute(
                    apiAddress
                )
            }
        } catch (e: Exception) {

        }
    }
}