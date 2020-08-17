package com.aleksanderkapera.covidstats.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.aleksanderkapera.covidstats.CovidStatsApp
import com.aleksanderkapera.covidstats.R
import com.aleksanderkapera.covidstats.databinding.ActivityMainBinding
import com.aleksanderkapera.covidstats.service.RefreshWidgetWorker

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        if (intent.action == Intent.ACTION_BUG_REPORT) {
            val request = OneTimeWorkRequestBuilder<RefreshWidgetWorker>().build()
            WorkManager.getInstance().enqueue(request)
        } else {
            super.onCreate(savedInstanceState)
            setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        }
    }
}