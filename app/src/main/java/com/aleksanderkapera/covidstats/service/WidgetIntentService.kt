package com.aleksanderkapera.covidstats.service

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.IBinder
import androidx.core.app.JobIntentService
import com.aleksanderkapera.covidstats.CovidStatsApp
import com.aleksanderkapera.covidstats.repository.StatsRepository
import com.aleksanderkapera.covidstats.room.AllStatusStatisticTable
import com.aleksanderkapera.covidstats.room.StatsDatabase
import com.aleksanderkapera.covidstats.ui.LatestStatsWidget.Companion.sendRefreshWidgetIntent
import com.aleksanderkapera.covidstats.ui.LatestStatsWidget.Companion.startSpinner
import com.aleksanderkapera.covidstats.ui.LatestStatsWidget.Companion.updateDisplayableStats
import com.aleksanderkapera.covidstats.util.SERVICE_WIDGET_INTENT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.random.Random

class WidgetIntentService : JobIntentService() {

    private val database = StatsDatabase.getInstance(CovidStatsApp.context)
    private val repository = StatsRepository.getInstance(database)
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override fun getApplicationContext(): Context {
        return super.getApplicationContext()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val widgetId = 105
        startSpinner(widgetId)
        if (intent?.action == SERVICE_WIDGET_INTENT) {
            val random = Random.nextInt(0, 420).toLong()
            updateDisplayableStats(
                widgetId,
                AllStatusStatisticTable(
                    0,
                    1597536000000,
                    "Poland",
                    "PL",
                    "",
                    "",
                    "",
                    "",
                    "",
                    random,
                    random,
                    random,
                    random
                )
            )
            sendRefreshWidgetIntent()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    override fun onHandleWork(intent: Intent) {
        val widgetId = 105
        startSpinner(widgetId)
        if (intent.action == SERVICE_WIDGET_INTENT) {
            val random = Random.nextInt(0, 420).toLong()
            updateDisplayableStats(
                widgetId,
                AllStatusStatisticTable(
                    0,
                    1597536000000,
                    "Poland",
                    "PL",
                    "",
                    "",
                    "",
                    "",
                    "",
                    random,
                    random,
                    random,
                    random
                )
            )
            sendRefreshWidgetIntent()
        }
    }

    companion object {
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, WidgetIntentService::class.java, 420, intent)
        }
    }

//    override fun onHandleIntent(intent: Intent?) {
//        val widgetId = 103
//        startSpinner(widgetId)
//        if (intent?.action == SERVICE_WIDGET_INTENT) {
//            val random = Random.nextInt(0, 420).toLong()
//            updateDisplayableStats(
//                widgetId,
//                AllStatusStatisticTable(
//                    0,
//                    1597536000000,
//                    "Poland",
//                    "PL",
//                    "",
//                    "",
//                    "",
//                    "",
//                    "",
//                    random,
//                    random,
//                    random,
//                    random
//                )
//            )
//            sendRefreshWidgetIntent()
//        }

//        if (intent?.action == SERVICE_WIDGET_INTENT) {
//            val widgetId = intent.getIntExtra(R.string.intent_refresh_id.asString(), 0)
//            val countryCode = intent.getStringExtra(R.string.intent_refresh_countryCode.asString())
//
//            scope.launch {
//                try {
//                    startSpinner(widgetId)
//                    sendRefreshWidgetIntent()
//                    repository.updateStats()
//                    delay(WIDGET_MIN_SPINNING_TIME)
//                } catch (t: Throwable) {
//                    Log.e(WidgetIntentService::class.simpleName, t.toString())
//                } finally {
//                    val todayStats =
//                        SharedPrefsManager.getList<AllStatusStatisticTable>(R.string.prefs_latest_stats.asString())
//                    updateDisplayableStats(
//                        widgetId,
//                        todayStats?.find { it.countryCode == countryCode })
//                    stopSpinner(widgetId)
//                    sendRefreshWidgetIntent()
//                }
//            }
//        }
//    }
}