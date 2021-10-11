package com.radio.streaming.activities

import android.app.Application

class StreamingApplication : Application() {
    companion object {
        lateinit var instance: StreamingApplication private set
    }

    override fun onCreate() {
        super.onCreate()

        instance = this
    }
}