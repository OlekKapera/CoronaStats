package com.aleksanderkapera.covidstats

import android.app.Application
import com.aleksanderkapera.covidstats.domain.Country
import com.aleksanderkapera.covidstats.util.LiveSharedPreferences
import com.aleksanderkapera.covidstats.util.SharedPrefsManager
import com.aleksanderkapera.covidstats.util.asString
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
        context = this

        JodaTimeAndroid.init(this)
        SharedPrefsManager.with(this)
        LiveSharedPreferences.with(this)

        initDefaultValues()
    }

    private fun initDefaultValues() {
        if (SharedPrefsManager.getList<Country>(R.string.prefs_chosen_countries.asString()) == null)
            SharedPrefsManager.putList<Country>(
                listOf(Country("Poland", "poland", "PL")),
                R.string.prefs_chosen_countries.asString()
            )
    }
}