package com.example.covidstats.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CountryTable::class, StatisticTable::class], version = 2)
abstract class StatsDatabase() : RoomDatabase() {
    abstract val statsDao: StatsDao
}

private lateinit var INSTANCE: StatsDatabase

fun getDatabase(context: Context): StatsDatabase {
    synchronized(StatsDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context, StatsDatabase::class.java, "stats")
                .fallbackToDestructiveMigration()
                .build()
        }
    }
    return INSTANCE
}