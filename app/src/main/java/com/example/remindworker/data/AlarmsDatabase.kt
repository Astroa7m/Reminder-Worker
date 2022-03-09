package com.example.remindworker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [AlarmsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AlarmsDatabase : RoomDatabase() {

    abstract fun getAlarmsDao(): AlarmsDao

    companion object {
        @Volatile
        private var INSTANCE: AlarmsDatabase? = null

        fun getInstance(context: Context): AlarmsDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AlarmsDatabase::class.java,
                        "alarms_db"
                    ).build()

                    INSTANCE = instance

                }
                return instance
            }
        }

    }


}