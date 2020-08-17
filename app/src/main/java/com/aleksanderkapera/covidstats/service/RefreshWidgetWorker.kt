package com.aleksanderkapera.covidstats.service

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.aleksanderkapera.covidstats.CovidStatsApp
import com.aleksanderkapera.covidstats.repository.StatsRepository
import com.aleksanderkapera.covidstats.room.AllStatusStatisticTable
import com.aleksanderkapera.covidstats.room.StatsDatabase
import com.aleksanderkapera.covidstats.ui.LatestStatsWidget
import com.aleksanderkapera.covidstats.util.SERVICE_WIDGET_INTENT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.random.Random

class RefreshWidgetWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    private val database = StatsDatabase.getInstance(CovidStatsApp.context)
    private val repository = StatsRepository.getInstance(database)
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override fun doWork(): Result {
        val widgetId = 98
        LatestStatsWidget.startSpinner(widgetId)
        val random = Random.nextInt(0, 420).toLong()
        LatestStatsWidget.updateDisplayableStats(
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
        LatestStatsWidget.sendRefreshWidgetIntent()

        return Result.success()
    }
}