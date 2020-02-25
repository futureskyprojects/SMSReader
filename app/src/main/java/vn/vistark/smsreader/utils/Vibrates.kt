package vn.vistark.smsreader.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

class Vibrates {
    companion object {
        fun tapVibrate(context: Context) {
            val vibrateTime = 80.toLong()
            // Thực hiện rung
            val vb = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vb.vibrate(
                    VibrationEffect.createOneShot(
                        vibrateTime,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                vb.vibrate(vibrateTime)
            }
        }

        fun remindVibrate(context: Context) {
            val vibrateTimes = longArrayOf(0, 300, 500, 400, 500, 500, 500)
            // Thực hiện rung
            val vb = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vb.vibrate(
                    VibrationEffect.createWaveform(vibrateTimes, -1)
                )
            } else {
                vb.vibrate(vibrateTimes, -1)
            }
        }
    }
}