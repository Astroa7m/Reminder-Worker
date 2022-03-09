package com.example.remindworker

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.remindworker.data.AlarmsDatabase
import com.example.remindworker.data.AlarmsEntity
import com.example.remindworker.extras.AlarmReceiver
import com.example.remindworker.extras.MAIN_OPENED_ALARM
import com.example.remindworker.extras.NOTIFICATION_OPEN_ACTION
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // if the user clicks on a reminder notification, then delete if from the db
        if (intent?.action.equals(NOTIFICATION_OPEN_ACTION)) {
          lifecycleScope.launchWhenStarted {
              AlarmsDatabase.getInstance(this@MainActivity).getAlarmsDao().deleteAlarm(
                  intent?.getStringExtra(
                      MAIN_OPENED_ALARM
                  ) ?: ""
              )
          }
        }

        setContent {
            val coroutineScope = rememberCoroutineScope()

            var showShowDatePicker by rememberSaveable {
                mutableStateOf(false)
            }

            var showShowTimePicker by rememberSaveable {
                mutableStateOf(false)
            }

            var remindedItemIndex by rememberSaveable {
                mutableStateOf(-1)
            }

            val alarmManager = remember {
                getSystemService(Context.ALARM_SERVICE) as AlarmManager
            }

            var t1 by remember { mutableStateOf(Triple(0, 0, 0)) }
            var t2 by remember { mutableStateOf(Pair(0, 0)) }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ListTasks(modifier = Modifier.fillMaxSize(), onItemLongClicked = {
                    showShowDatePicker = true
                    remindedItemIndex = it
                })



                CalendarDatePicker(
                    isNotDismissed = showShowDatePicker,
                    onDateChosen = { calendarResult ->
                        showShowDatePicker = false
                        showShowTimePicker = true
                        t1 = calendarResult
                        Log.d("DEBUG_TAG", "onCreate: date = $calendarResult")
                    }) {
                    remindedItemIndex = -1
                    showShowDatePicker = false
                    showShowTimePicker = false
                }

                TimePicker(isNotDismissed = showShowTimePicker, onTimeChosen = { timeResult ->
                    showShowTimePicker = false
                    t2 = timeResult

                    val timeMillis = Calendar.getInstance().apply {
                        set(
                            t1.first,
                            t1.second,
                            t1.third,
                            t2.first,
                            t2.second
                        )
                    }.timeInMillis

                    setAlarm(listOfTasks[remindedItemIndex]) {
                        //Note: using exact time is discouraged as it affects system resources such as battery life
                        //im using it here for my own practical purpose only


                        //I'm saving the timeMillis in db to reschedule them after reboot
                        coroutineScope.launch {
                            AlarmsDatabase.getInstance(this@MainActivity).getAlarmsDao()
                                .insertAlarm(
                                    AlarmsEntity(
                                        0,
                                        timeMillis,
                                        listOfTasks[remindedItemIndex]
                                    )
                                )
                        }
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP, timeMillis, it
                        )
                    }

                }) {
                    showShowTimePicker = false
                }
            }
        }
    }

    private inline fun setAlarm(textString: String, onSetting: (PendingIntent) -> Unit) {
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("text", textString)
        }
        val pendingIntent =
            PendingIntent.getBroadcast(this, textString.hashCode(), intent, FLAG_IMMUTABLE)

        onSetting.invoke(pendingIntent)

        Toast.makeText(this, "Reminder's set", Toast.LENGTH_SHORT).show()
    }

    // Unused cuz I wont cancel any
    private inline fun cancelAlarm(textString: String, onCancelling: (PendingIntent) -> Unit) {
        // NOT TODO
    }

}

