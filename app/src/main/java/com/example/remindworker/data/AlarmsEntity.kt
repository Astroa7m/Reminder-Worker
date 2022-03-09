package com.example.remindworker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AlarmsEntity(
    @PrimaryKey(autoGenerate = true)
    val alarmId: Int,
    val alarmTimeMillis: Long,
    val alarmsText: String
)
