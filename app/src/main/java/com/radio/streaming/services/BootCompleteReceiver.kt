package com.radio.streaming.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.radio.streaming.activities.MainActivity
import java.io.IOException

class BootCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        when(intent?.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                context.startActivity(
                    Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                MainActivity.createRebootAlarm(context)
            }
            "REBOOT" -> {
                try {
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "reboot")).waitFor()
                } catch (e: IOException) {
                }
            }
        }
    }
}