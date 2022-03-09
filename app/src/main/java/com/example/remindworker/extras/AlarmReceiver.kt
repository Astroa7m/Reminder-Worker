package com.example.remindworker.extras

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.remindworker.MainActivity
import com.example.remindworker.R
import com.example.remindworker.data.AlarmsDatabase
import com.example.remindworker.data.AlarmsEntity
import kotlinx.coroutines.*

/**
 * Basically, here I'm checking different intents' actions sent from a notification, alarm manager, or system itself
 * 1- ACTION_BOOT_COMPLETED: system notify the app when a reboot has been performed successfully. When it is done a reschedule all the
 *      alarms from app database so the user wont lose any alarms
 * 2- NOTIFICATION_DISMISSED_ACTION: custom action notifying the app that a reminder notification has been dismissed by the user indicating
 *      that the user does not want to be reminded again of the same task. Therefore it should be deleted from our database and never remind
 *      the user again with this task.
 * 3- Not dismissed and not rebooted: if the action does not match any of our cases then the user's setting an alarm and the app has to show a
 *      a proper notification at the specified time.
 */

const val NOTIFICATION_DISMISSED_ACTION = "notification_dismissed"
const val NOTIFICATION_OPEN_ACTION = "notification_opened"
const val DISMISSED_ALARM = "notification_opened"
const val MAIN_OPENED_ALARM = "notification_opened"

class AlarmReceiver : BroadcastReceiver() {

    private var coroutineScope: CoroutineScope? = null

    override fun onReceive(ctx: Context?, i: Intent?) {
        when (i?.action) {
            Intent.ACTION_BOOT_COMPLETED -> getAllAndResetAlarms(ctx!!)
            NOTIFICATION_DISMISSED_ACTION -> removeAlarmFromDb(ctx!!, i.getStringExtra(DISMISSED_ALARM) ?: "")
            else -> createNotification(ctx!!, i)
        }
    }

    private fun removeAlarmFromDb(ctx: Context, text: String) {
        if (coroutineScope == null)
            coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        coroutineScope!!.launch {
            //im deleting by alarms text, it's better to delete by id instead as the user
            //may have two alarms with the same text.
            AlarmsDatabase.getInstance(ctx).getAlarmsDao().deleteAlarm(text)
            coroutineScope!!.cancel()
        }
    }

    private fun getAllAndResetAlarms(ctx: Context) {
        var alarms: List<AlarmsEntity>? = null
        if (coroutineScope == null)
            coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        coroutineScope!!.launch {
            launch {
                alarms = AlarmsDatabase.getInstance(ctx).getAlarmsDao().getAllAlarms()
            }.join()
            launch {
                if (!alarms.isNullOrEmpty()) {
                    val alarmManager =
                        ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    for (alarm in alarms!!) {
                        val intent = Intent(ctx, AlarmReceiver::class.java).apply {
                            putExtra("text", alarm.alarmsText)
                            action = "ALARM_ACTION"
                        }

                        val pendingIntent =
                            PendingIntent.getBroadcast(
                                ctx, alarm.alarmsText.hashCode(), intent,
                                PendingIntent.FLAG_IMMUTABLE
                            )

                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP, alarm.alarmTimeMillis, pendingIntent
                        )
                    }
                }
            }
            coroutineScope?.cancel()
        }
    }

    private fun createNotification(ctx: Context, i: Intent?) {
        val text = i?.getStringExtra("text")

        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action = NOTIFICATION_OPEN_ACTION
            putExtra(MAIN_OPENED_ALARM, text)
        }

        val pendingIntent =
            PendingIntent.getActivity(ctx, text.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE)


        val dismissalIntent = Intent(ctx, AlarmReceiver::class.java).apply {
            action = NOTIFICATION_DISMISSED_ACTION
            putExtra(DISMISSED_ALARM, text)
        }
        //I'll use to to detect dismissal of notification to remove alarm from db
        val dismissalPendingIntent =
            PendingIntent.getBroadcast(ctx, text.hashCode(), dismissalIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT)

        val notification = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Reminder")
            .setContentText(text)
            .setDeleteIntent(dismissalPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(ctx)) {
            notify(text.hashCode(), notification.build())
        }
    }
}