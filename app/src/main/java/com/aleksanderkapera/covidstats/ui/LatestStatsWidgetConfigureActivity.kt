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
import com.aleksanderkapera.covidstats.room.AllStatusStatisticTable
import com.aleksanderkapera.covidstats.util.InjectorUtils
import com.aleksanderkapera.covidstats.util.SharedPrefsManager
import com.aleksanderkapera.covidstats.util.asString
import com.aleksanderkapera.covidstats.viewmodel.ChooseCountryDialogViewModel

class LatestStatsWidgetConfigureActivity : AppCompatActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var viewModel: ChooseCountryDialogViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            InjectorUtils.provideChooseCountryDialogViewModelFactory(CovidStatsApp.context)
        ).get(ChooseCountryDialogViewModel::class.java)

        ChooseCountryDialog(ChooseCountryDialog.Mode.WIDGET).show(
            supportFragmentManager,
            R.string.dialog_choose_country.asString()
        )

        val extras = intent.extras
        appWidgetId = extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        viewModel.onPositiveButtonClickEvent.observe(this, Observer { isClicked ->
            if (isClicked) {
                val countryToDisplay =
                    SharedPrefsManager.getList<AllStatusStatisticTable>(R.string.prefs_latest_stats.asString())
                        ?.find { statistic ->
                            viewModel.clickedCountries.value?.find { country ->
                                statistic.countryCode == country.iso2
                            } != null
                        }
                countryToDisplay?.let {
                    SharedPrefsManager.put<AllStatusStatisticTable>(
                        countryToDisplay,
                        R.string.prefs_widget_content.asString() + appWidgetId
                    )
                    LatestStatsWidget.sendRefreshWidgetIntent()
                }

                viewModel.finishOnPositiveButtonClick()
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