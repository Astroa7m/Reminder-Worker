package com.example.remindworker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AlarmsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmsEntity)

    @Query("DELETE FROM AlarmsEntity WHERE alarmsText = :alarmString")
    suspend fun deleteAlarm(alarmString: String)

    @Query("SELECT * FROM AlarmsEntity")
    suspend fun getAllAlarms() : List<AlarmsEntity>
}