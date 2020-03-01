package vn.vistark.smsreader

import android.Manifest
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.no_internet.*
import kotlinx.android.synthetic.main.pay_money.*
import org.json.JSONObject
import vn.vistark.smsreader.services.BackgroundRunningService
import vn.vistark.smsreader.ui.Config
import vn.vistark.smsreader.ui.setting_activity.SettingActivity
import vn.vistark.smsreader.utils.ServicesUtils
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        var res = ""
        var isTry = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ConfigStateExtract().execute().get()) {
            // Khi đã có kết nối internet, ẩn bảng thông báo về vấn đề này đi
            rlNoInternet.visibility = View.GONE
            // Kiểm tra thuộc tính lấy được xem người dùng có đang dùng thử hay không?
            val obj = JSONObject(res)
            isTry = obj.getBoolean("isTry")
            if (obj.getBoolean("requestPayFee")) {
                if (ServicesUtils.isMyServiceRunning(this, BackgroundRunningService::class.java)) {
                    // Ngắt dịch vụ đang chạy
                    stopService(Intent(this, BackgroundRunningService::class.java))
                }
                // Nếu là dùng thử thì hiển thị bảng yêu cầu thanh toán
                rlPayMoney.visibility = View.VISIBLE
            } else {
                // Tiến hành ẩn bảng dùng thử
                rlPayMoney.visibility = View.GONE
                // Khởi động trang thiết lập chính
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        startActivity(Intent(this@MainActivity, SettingActivity::class.java))
                        finish()
                    }
                }, 500)
            }
        } else {
            // Nếu không lấy được nội dung, thì đây là do việc không có kết nối internet
            rlNoInternet.visibility = View.VISIBLE
        }
    }

    class ConfigStateExtract : AsyncTask<Void, Void, Boolean>() {
        override fun doInBackground(vararg params: Void?): Boolean {
            try {
                val httpCon = URL(Config.configStateUrl).openConnection() as HttpURLConnection
                httpCon.connectTimeout = 1000
                httpCon.readTimeout = 1000
                res = URL(Config.configStateUrl).readText()
            } catch (e: Exception) {
                return false
            }
            return true
        }
    }
}
