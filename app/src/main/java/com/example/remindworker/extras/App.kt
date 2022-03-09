package com.example.remindworker.extras

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.graphics.Color

/**
 * Creating notification channel
 */

const val CHANNEL_ID = "channelId"
private const val CHANNEL_NAME = "Task Reminder Channel"
private const val CHANNEL_DESCRIPTION = "notify users about alarms that they set"

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        //Note: notification channels are only required for Android >=26
        // current minSdk is 26 so no need to check the sdk version
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESCRIPTION
            enableLights(true)
            lightColor = Color.RED
        }

        with(getSystemService(NOTIFICATION_SERVICE) as NotificationManager){
            createNotificationChannel(channel)
        }

    }
}