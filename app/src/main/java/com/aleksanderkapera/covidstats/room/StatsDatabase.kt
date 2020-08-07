package com.aleksanderkapera.covidstats.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CountryTable::class, AllStatusStatisticTable::class], version = 5)
abstract class StatsDatabase() : RoomDatabase() {
    abstract fun statsDao(): StatsDao
    abstract fun countriesDao(): CountriesDao

    companion object {

        @Volatile
        private var instance: StatsDatabase? = null

        fun getInstance(context: Context): StatsDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): StatsDatabase {
            return Room.databaseBuilder(context, StatsDatabase::class.java, "stats")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
        }
    }
}