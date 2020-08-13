package com.aleksanderkapera.covidstats.util

import android.content.Context
import com.aleksanderkapera.covidstats.repository.StatsRepository
import com.aleksanderkapera.covidstats.room.StatsDatabase
import com.aleksanderkapera.covidstats.viewmodel.ChooseCountryDialogViewModelFactory
import com.aleksanderkapera.covidstats.viewmodel.MainFragmentViewModelFactory

object InjectorUtils {

    private fun getStatsRepository(context: Context): StatsRepository {
        return StatsRepository.getInstance(
            StatsDatabase.getInstance(context.applicationContext)
        )
    }

    fun provideMainFragmentViewModelFactory(context: Context): MainFragmentViewModelFactory {
        return MainFragmentViewModelFactory(getStatsRepository(context))
    }

    fun provideChooseCountryDialogViewModelFactory(context: Context): ChooseCountryDialogViewModelFactory {
        return ChooseCountryDialogViewModelFactory(getStatsRepository(context))
    }
}