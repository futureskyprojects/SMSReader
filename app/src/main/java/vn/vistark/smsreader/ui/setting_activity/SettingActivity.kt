package vn.vistark.smsreader.ui.setting_activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_setting.*
import vn.vistark.smsreader.MainActivity
import vn.vistark.smsreader.R
import vn.vistark.smsreader.services.BackgroundRunningService
import vn.vistark.smsreader.utils.HttpPost
import vn.vistark.smsreader.utils.ServicesUtils

class SettingActivity : AppCompatActivity() {
    companion object {
        val PREFS_NAME = "VISTARK_SMS_READER"
        var API_ADDRESS = "API_ADDRESS"
//        var PHONE_FILTER = "PHONE_FILTER"
//        var SECURITY_CODE = "SECURITY_CODE"
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        // Thiết lập yêu cầu các quyền cần thiết
        requestPermission()

        val prefs = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        try {
            edtAPI.setText(prefs.getString(API_ADDRESS, ""))
            if (edtAPI.text.isEmpty()) {
                edtAPI.setText("http://bm88.vn/api/put_sms_banking.php/pay/?key=fwyTxVXjsMfKS50V8il23nPP0OzvPxaM")
                btnSaveSetting.performClick();
            }
        } catch (e: Exception) {
            // Bỏ qua lỗi
        }

        // Nếu là thử nghiệm
        if (MainActivity.isTry) {
            btnTry.visibility = View.VISIBLE
            btnTry.setOnClickListener {
                HttpPost(this, -1).execute(
                    edtAPI.text.toString(),
                    "SD TK 0491000098157 +2,000,000VND luc 28-02-2020 15:15:08. SD 2,583,265VND. Ref MBVCB351476977.SMS xuanhuantb.CT tu 0301000324128 DAO THI..."
                )
            }
        } else {
            btnTry.visibility = View.GONE
        }
        ///////////////

        initSaveBtnEvents(prefs)
        initServicesBtnEvents()
        runIfServiceWasNot()
        updateServicesBtn()
    }

    private fun runIfServiceWasNot() {
        // Khởi chạy dịch vụ nếu trước đó chưa chạy
        if (!ServicesUtils.isMyServiceRunning(this, BackgroundRunningService::class.java)) {
            startService(Intent(this, BackgroundRunningService::class.java))
        }
    }

    private fun initServicesBtnEvents() {
        btnStartServices.setOnClickListener {
            startService(Intent(this, BackgroundRunningService::class.java))
            updateServicesBtn()
        }

        btnEndServices.setOnClickListener {
            stopService(Intent(this, BackgroundRunningService::class.java))
            updateServicesBtn()
        }
    }

    private fun initSaveBtnEvents(prefs: SharedPreferences) {
        btnSaveSetting.setOnClickListener {
            val api = edtAPI.text.toString()
            if (api.isBlank() || api.isEmpty()) {
                Toast.makeText(
                    this,
                    "Địa chỉ API là bắt buộc và không được bỏ trống",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (!api.contains("http://") && !api.contains("https://") && !api.contains(".")) {
                Toast.makeText(this, "Địa chỉ API không hợp lệ.", Toast.LENGTH_SHORT).show()
            } else {
                val editor = prefs.edit()
                editor.putString("API_ADDRESS", api)
                editor.apply()

                Toast.makeText(this, "Đã lưu thành công!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.INTERNET,
                    Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.READ_PHONE_NUMBERS,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_CONTACTS
                ),
                1998
            )
        }
    }

    private fun updateServicesBtn() {
        if (ServicesUtils.isMyServiceRunning(this, BackgroundRunningService::class.java)) {
            btnStartServices.visibility = View.GONE
            btnEndServices.visibility = View.VISIBLE
        } else {
            btnStartServices.visibility = View.VISIBLE
            btnEndServices.visibility = View.GONE
        }
    }
}
