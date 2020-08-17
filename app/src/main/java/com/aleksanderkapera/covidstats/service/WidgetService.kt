package com.aleksanderkapera.covidstats.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import com.aleksanderkapera.covidstats.room.AllStatusStatisticTable
import com.aleksanderkapera.covidstats.ui.LatestStatsWidget
import com.aleksanderkapera.covidstats.util.MSG_REFRESH_WIDGET
import kotlin.random.Random

class WidgetService : Service() {

    override fun onBind(intent: Intent): IBinder {
        return Messenger(ServiceHandler(this)).binder
    }

    internal class ServiceHandler(
        context: Context,
        private val applicationContext: Context = context.applicationContext
    ) : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == MSG_REFRESH_WIDGET) {
                val widgetId = 103
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
            } else
                super.handleMessage(msg)
        }
    }
}
