package com.radio.streaming.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager
import com.radio.streaming.R
import com.radio.streaming.databinding.ActivityMainBinding
import com.radio.streaming.services.BootCompleteReceiver
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var mPlayer: SimpleExoPlayer
    private lateinit var mBinding: ActivityMainBinding

    private val handler = Handler(Looper.getMainLooper())
    private var ledGpio: Gpio? = null
    private var ledState = false
    private val blinkRunnable = object : Runnable {
        override fun run() {
            ledState = !ledState
            ledGpio!!.value = ledState
            handler.postDelayed(this, 1000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mPlayer = SimpleExoPlayer.Builder(this).build()

        mBinding.playerView.player = mPlayer
        mBinding.btnPrepare.setOnClickListener {
            mPlayer.setMediaItem(MediaItem.fromUri(mBinding.edtUrl.text.toString()))
            mPlayer.prepare()
            mPlayer.seekToDefaultPosition()
        }

        val sharedPref = getSharedPref()
        if (sharedPref.getLong(PREF_INSTALL_APP_TIME, 0L) == 0L) {
            sharedPref.edit().putLong(PREF_INSTALL_APP_TIME, Calendar.getInstance().timeInMillis).apply()
            createRebootAlarm(this)
        }

        val gpioForLED = when (Build.DEVICE) {
            "rpi3", "rpi3bp" -> "BCM6"
            "imx7d_pico" -> "GPIO2_IO02"
            else -> {
                showToast("Unknown Build.DEVICE ${Build.DEVICE}")
                return
            }
        }
        ledGpio = PeripheralManager.getInstance().openGpio(gpioForLED)
        ledGpio!!.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
        handler.post(blinkRunnable)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val PREF_INSTALL_APP_TIME = "INSTALL_APP_TIME"

        fun getSharedPref() = StreamingApplication.instance.getSharedPreferences("RadioStreaming2021", MODE_PRIVATE)!!

        fun createRebootAlarm(context: Context) {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                add(Calendar.DATE, 1)
            }
            val alarmIntent = Intent(context, BootCompleteReceiver::class.java).setAction("REBOOT")
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
}