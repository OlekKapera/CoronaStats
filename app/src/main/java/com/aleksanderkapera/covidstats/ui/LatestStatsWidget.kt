package com.aleksanderkapera.covidstats.ui

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.aleksanderkapera.covidstats.R
import com.aleksanderkapera.covidstats.util.asString

/**
 * Implementation of App Widget functionality.
 */
class LatestStatsWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(
                context,
                appWidgetManager,
                appWidgetId
            )
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {

        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val widgetText = context.getString(R.string.appwidget_text)
            // Construct the RemoteViews object
            val views = RemoteViews(
                context.packageName,
                R.layout.widget_latest_stats
            )
//            views.setOnClickPendingIntent(R.id.widget_image_refresh, getPendingIntent(context))

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun getPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, MainActivity::class.java)
            intent.action = R.string.intent_refresh_stats.asString()
            return PendingIntent.getActivity(context, 100, intent, 0)
        }
    }
}