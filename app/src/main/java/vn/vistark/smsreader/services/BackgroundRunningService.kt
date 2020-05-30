package vn.vistark.smsreader.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_setting.*
import org.json.JSONObject
import vn.vistark.smsreader.MainActivity
import vn.vistark.smsreader.R
import vn.vistark.smsreader.data.db.MsgHandler
import vn.vistark.smsreader.ui.setting_activity.SettingActivity
import vn.vistark.smsreader.utils.HttpPost
import java.util.*

class BackgroundRunningService : Service() {
    lateinit var prefs: SharedPreferences
    val INTERVAL_TIME = 30000.toLong()
    val PERIOD = 5000.toLong()
    // Phần khai báo liên quan đến thông báo (Notification)
    private val mNotificationChannelId = "setting"
    private val mNotificationId = 123
    lateinit var mHandler: Handler
    private var isSync = false

    override fun onCreate() {
        super.onCreate()
        prefs = this.getSharedPreferences(SettingActivity.PREFS_NAME, Context.MODE_PRIVATE)

    }

    protected fun onHandleIntent(msg: String) {
        mHandler.post {
            Toast.makeText(
                this,
                msg,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private lateinit var timer: Timer

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mHandler = Handler()
        val notiManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // A. Tạo notification channel cho android phiên bản từ O đổ lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    mNotificationChannelId,
                    "Cài đặt",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    setShowBadge(true)
                }
            channel.lightColor = Color.BLUE
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

            notiManager.createNotificationChannel(channel)
        }

        // B. Tạo pendingIntent cho notify
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        // c. Hiển thị noti và chạy services ngầm
        val notification: Notification = NotificationCompat.Builder(this, mNotificationChannelId)
            .setContentTitle("SMS Reader")
            .setContentText("Đang đợi để xử lý tin nhắn đến")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notiManager.notify(mNotificationId, notification)

        // C. Tiến hành chạy Forefround (chạy dưới nền)
        startForeground(mNotificationId, notification)

        // Thực hiện các tác vụ tại đây
        syncSmsTask()
        return START_STICKY
    }

    private fun syncSmsTask() {
        timer = Timer()
        var counter = 0
        timer.schedule(object : TimerTask() {
            override fun run() {
                // Khu vực check xem có yêu cầu phải trả phí hay không
                counter++;
                if (counter == (INTERVAL_TIME / PERIOD).toInt()) {
                    counter = 0
                    if (MainActivity.ConfigStateExtract().execute().get()) {
                        val obj = JSONObject(MainActivity.res)
                        if (obj.getBoolean("requestPayFee")) {
                            onDestroy()
                            stopSelf()
                        }
                    }
                }

                // Khu vực đồng bộ tin nhắn
                if (!isSync) {
                    try {
                        isSync = true
                        val apiAddress = prefs.getString(SettingActivity.API_ADDRESS, "")
                        val msgs = MsgHandler(this@BackgroundRunningService).getAll()
                        if (msgs.size > 0) {
                            for (msg in msgs) {
                                HttpPost(this@BackgroundRunningService, msg).execute(
                                    apiAddress
                                )
                            }
                        }
                        isSync = false
                    } catch (e: java.lang.Exception) {
                        isSync = false
                    }
                }
            }
        }, 1000, PERIOD)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
    }
}