package com.aleksanderkapera.covidstats.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.aleksanderkapera.covidstats.CovidStatsApp
import com.aleksanderkapera.covidstats.R
import com.aleksanderkapera.covidstats.repository.StatsRepository
import com.aleksanderkapera.covidstats.room.StatsDatabase
import com.aleksanderkapera.covidstats.util.asString
import com.aleksanderkapera.covidstats.viewmodel.MainFragmentViewModel
import com.aleksanderkapera.covidstats.viewmodel.MainFragmentViewModelFactory

class LatestStatsWidgetConfigureActivity : AppCompatActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private val database = StatsDatabase.getInstance(CovidStatsApp.context)
    private val repository = StatsRepository.getInstance(database)
    private lateinit var viewModel: MainFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            MainFragmentViewModelFactory(repository)
        ).get(MainFragmentViewModel::class.java)

        ChooseCountryDialog(viewModel).show(
            supportFragmentManager,
            R.string.dialog_choose_country.asString()
        )

        val extras = intent.extras
        appWidgetId = extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        viewModel.userCountries.observe(this, Observer {
            viewModel.updateTodayStats()

            if (viewModel.chooseCountryDialogEvent) {
                viewModel.updateStats()
                viewModel.finishCountryDialogChosen()
                close()
            }
        })

        setResult(Activity.RESULT_CANCELED)
    }

    private fun close() {
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }
}