package com.example.remindworker

import android.widget.CalendarView
import android.widget.TimePicker
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import java.util.*

var listOfTasks = mutableListOf(
    "Talk a walk",
    "Move the trash",
    "Do my homework",
    "Grab so dinner",
    "Call my family",
    "Drink Coffee",
    "Study",
    "Listen to some rock music",
    "Play video games",
    "Watch anime",
    "read a book",
    "Play chess",
    "Practice Python",
    "Create self-projects",
    "Hangout with friends",
    "Do some workouts",
    "Make designs",
    "Practice coding",
    "Take a shower",
    "Watch Youtube",
    "Contribute to open-source projects",
    "Learn Kali"
).shuffled()

@Composable
fun TimePicker(
    isNotDismissed: Boolean,
    onTimeChosen: (Pair<Int, Int>) -> Unit,
    onCancelled: () -> Unit
) {
    val c = remember { Calendar.getInstance() }
    var hourValue = remember { c.get(Calendar.HOUR) }
    var minuteValue = remember { c.get(Calendar.MINUTE) }

    if (isNotDismissed) {
        Dialog(onDismissRequest = { onCancelled() }) {
            Surface(
                modifier = Modifier
                    .wrapContentSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colors.background)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Set time for the reminder",
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AndroidView(
                        factory = { context ->
                            TimePicker(context).apply {
                                setIs24HourView(false)
                            }
                        },
                        Modifier.wrapContentSize(),
                        update = { view ->
                            view.setOnTimeChangedListener { _, hour, minute ->
                                hourValue = hour
                                minuteValue = minute
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(onClick = {
                            onTimeChosen(Pair(hourValue, minuteValue))
                        }) {
                            Text(text = "OK")
                        }
                        OutlinedButton(onClick = {
                            onCancelled()
                        }) {
                            Text(text = "Cancel")
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun CalendarDatePicker(
    isNotDismissed: Boolean,
    onDateChosen: (Triple<Int, Int, Int>) -> Unit,
    onCancelled: () -> Unit
) {

    val c = remember { Calendar.getInstance() }
    var yearValue = remember { c.get(Calendar.YEAR) }
    var monthValue = remember { c.get(Calendar.MONTH) }
    var dayValue = remember { c.get(Calendar.DAY_OF_MONTH) }

    if (isNotDismissed) {
        Dialog(onDismissRequest = { onCancelled() }) {
            Surface(
                modifier = Modifier
                    .wrapContentSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colors.background)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Set date for the reminder",
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AndroidView(
                        factory = { context ->
                            CalendarView(context)
                        },
                        Modifier.wrapContentSize(),
                        update = { view ->
                            view.setOnDateChangeListener { _, year, month, day ->
                                yearValue = year
                                monthValue = month
                                dayValue = day
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(onClick = {
                            onDateChosen(
                                Triple(
                                    yearValue,
                                    monthValue,
                                    dayValue
                                )
                            )
                        }) {
                            Text(text = "OK")
                        }
                        OutlinedButton(onClick = {
                            onCancelled()
                        }) {
                            Text(text = "Cancel")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListTasks(
    modifier: Modifier = Modifier,
    onItemLongClicked: (Int) -> Unit
) {
    LazyColumn(modifier.padding(16.dp)) {
        itemsIndexed(listOfTasks) { index, item ->
            Text(
                text = item,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colors.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                onItemLongClicked(index)
                            }
                        )
                    }
            )
            if (index != listOfTasks.size - 1)
                Divider()
        }
    }
}
