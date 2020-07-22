package com.example.covidstats

import android.app.Application
import net.danlew.android.joda.JodaTimeAndroid

/**
 * Sets up background tasks with work manager
 */
class CovidStatsApp : Application() {

    override fun onCreate() {
        super.onCreate()
        JodaTimeAndroid.init(this)
    }
}