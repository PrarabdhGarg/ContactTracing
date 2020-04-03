/*
package com.example.covid_19.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
        entities = [],
        version = 1
)
abstract class RoomDatabse {

    companion object {
        @Volatile private var instance: RoomDatabse? = null
        private val lock = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(lock) {
            instance = (instance ?: buildDatabase(context)) as RoomDatabse?
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
                context,
                RoomDatabase::class.java,
                "covid_19.db"
        ).build()
    }

}*/
