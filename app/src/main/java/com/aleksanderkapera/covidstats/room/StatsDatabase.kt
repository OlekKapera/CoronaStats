package com.aleksanderkapera.covidstats.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CountryTable::class, AllStatusStatisticTable::class], version = 4)
abstract class StatsDatabase() : RoomDatabase() {
    abstract val statsDao: StatsDao
    abstract val countriesDao: CountriesDao
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