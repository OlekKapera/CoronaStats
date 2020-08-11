package com.aleksanderkapera.covidstats.ui

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import androidx.lifecycle.ViewModelProvider
import com.aleksanderkapera.covidstats.R
import com.aleksanderkapera.covidstats.util.InjectorUtils
import com.aleksanderkapera.covidstats.viewmodel.MainFragmentViewModel


class WidgetProvider : AppWidgetProvider() {

    private lateinit var mainViewModel: MainFragmentViewModel

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        mainViewModel = ViewModelProvider(
            context?.applicationContext,
            InjectorUtils.provideMainFragmentViewModelFactory(context ?: return)
        ).get(MainFragmentViewModel::class.java)

        appWidgetIds?.forEach { id ->
            val views = RemoteViews(context?.packageName, R.layout.latest_stats_widget)
            views.
        }

    }
}