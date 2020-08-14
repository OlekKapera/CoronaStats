package com.aleksanderkapera.covidstats.util

import android.content.Context
import com.aleksanderkapera.covidstats.repository.StatsRepository
import com.aleksanderkapera.covidstats.room.StatsDatabase
import com.aleksanderkapera.covidstats.viewmodel.ChooseCountryDialogViewModelFactory
import com.aleksanderkapera.covidstats.viewmodel.MainFragmentViewModelFactory

object InjectorUtils {

    fun getStatsDatabase(context: Context): StatsDatabase {
        return StatsDatabase.getInstance(context)
    }

    private fun getStatsRepository(context: Context): StatsRepository {
        return StatsRepository.getInstance(
            getStatsDatabase(context)
        )
    }

    fun provideMainFragmentViewModelFactory(context: Context): MainFragmentViewModelFactory {
        return MainFragmentViewModelFactory(getStatsRepository(context))
    }

    fun provideChooseCountryDialogViewModelFactory(context: Context): ChooseCountryDialogViewModelFactory {
        return ChooseCountryDialogViewModelFactory(getStatsRepository(context))
    }
}