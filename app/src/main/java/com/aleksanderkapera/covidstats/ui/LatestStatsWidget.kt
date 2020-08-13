package com.aleksanderkapera.covidstats.ui

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.aleksanderkapera.covidstats.CovidStatsApp
import com.aleksanderkapera.covidstats.R
import com.aleksanderkapera.covidstats.room.AllStatusStatisticTable
import com.aleksanderkapera.covidstats.service.WidgetIntentService
import com.aleksanderkapera.covidstats.util.DateStandardConverter
import com.aleksanderkapera.covidstats.util.SERVICE_WIDGET_INTENT
import com.aleksanderkapera.covidstats.util.SharedPrefsManager
import com.aleksanderkapera.covidstats.util.asString

/**
 * Implementation of App Widget functionality.
 */
class LatestStatsWidget : AppWidgetProvider() {

    private var statistic: AllStatusStatisticTable? = null

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            statistic =
                SharedPrefsManager.get<AllStatusStatisticTable>(R.string.prefs_widget_content.asString() + appWidgetId)

            updateAppWidget(
                context,
                statistic,
                appWidgetManager,
                appWidgetId
            )
        }
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)

        appWidgetIds?.forEach { id ->
            SharedPrefsManager.delete(R.string.prefs_widget_content.asString() + id)
        }
    }

    companion object {

        internal fun updateAppWidget(
            context: Context,
            statistic: AllStatusStatisticTable?,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(
                context.packageName,
                R.layout.widget_latest_stats
            )
            statistic?.let {
                views.apply {
                    setTextViewText(R.id.widget_text_newMain, statistic.confirmed.toString())
                    setTextViewText(R.id.widget_text_deathsMain, statistic.deaths.toString())
                    setTextViewText(R.id.widget_text_recoveredMain, statistic.recovered.toString())
                    setTextViewText(R.id.widget_text_country, statistic.countryName)
                    setTextViewText(
                        R.id.widget_text_date,
                        DateStandardConverter.print(statistic.date)
                    )
                }
            }

            views.setOnClickPendingIntent(R.id.widget_image_refresh, startService(context))

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun startService(context: Context): PendingIntent {
            val intent = Intent(context, WidgetIntentService::class.java)
            intent.action = SERVICE_WIDGET_INTENT
            return PendingIntent.getService(context, 100, intent, 0)
        }

        /**
         * Send intent to widget to update its data
         */
        fun sendRefreshWidgetIntent() {
            // Send a broadcast so that the Operating system updates the widget
            val man = AppWidgetManager.getInstance(CovidStatsApp.context)
            val ids =
                man.getAppWidgetIds(
                    ComponentName(
                        CovidStatsApp.context,
                        LatestStatsWidget::class.java
                    )
                )
            val updateIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            CovidStatsApp.context.sendBroadcast(updateIntent)
        }
    }
}