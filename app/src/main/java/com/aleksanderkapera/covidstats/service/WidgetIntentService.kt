package com.aleksanderkapera.covidstats.service

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.aleksanderkapera.covidstats.CovidStatsApp
import com.aleksanderkapera.covidstats.R
import com.aleksanderkapera.covidstats.repository.StatsRepository
import com.aleksanderkapera.covidstats.room.AllStatusStatisticTable
import com.aleksanderkapera.covidstats.room.StatsDatabase
import com.aleksanderkapera.covidstats.ui.LatestStatsWidget.Companion.sendRefreshWidgetIntent
import com.aleksanderkapera.covidstats.ui.LatestStatsWidget.Companion.startSpinner
import com.aleksanderkapera.covidstats.ui.LatestStatsWidget.Companion.stopSpinner
import com.aleksanderkapera.covidstats.ui.LatestStatsWidget.Companion.updateDisplayableStats
import com.aleksanderkapera.covidstats.util.SERVICE_WIDGET_INTENT
import com.aleksanderkapera.covidstats.util.SharedPrefsManager
import com.aleksanderkapera.covidstats.util.asString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WidgetIntentService : IntentService(SERVICE_WIDGET_INTENT) {

    private val database = StatsDatabase.getInstance(CovidStatsApp.context)
    private val repository = StatsRepository.getInstance(database)
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override fun onHandleIntent(intent: Intent?) {
        if (intent?.action == SERVICE_WIDGET_INTENT) {
            val widgetId = intent.getIntExtra(R.string.intent_refresh_id.asString(), 0)
            val countryCode = intent.getStringExtra(R.string.intent_refresh_countryCode.asString())

            scope.launch {
                try {
                    startSpinner(widgetId)
                    sendRefreshWidgetIntent()
                    repository.updateStats()
                } catch (t: Throwable) {
                    Log.e(WidgetIntentService::class.simpleName, t.toString())
                } finally {
                    val todayStats =
                        SharedPrefsManager.getList<AllStatusStatisticTable>(R.string.prefs_latest_stats.asString())
                    updateDisplayableStats(
                        widgetId,
                        todayStats?.find { it.countryCode == countryCode })
                    stopSpinner(widgetId)
                    sendRefreshWidgetIntent()
                }
            }
        }
    }
}