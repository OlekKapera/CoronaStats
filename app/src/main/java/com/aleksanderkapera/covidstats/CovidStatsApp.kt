package com.aleksanderkapera.covidstats

import android.app.Application
import com.aleksanderkapera.covidstats.util.SharedPrefsManager
import net.danlew.android.joda.JodaTimeAndroid

/**
 * Sets up background tasks with work manager
 */
class CovidStatsApp : Application() {

    //providing context for whole app
    companion object {
        lateinit var context: CovidStatsApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        JodaTimeAndroid.init(this)
        SharedPrefsManager.with(this)

        context = this
    }
}