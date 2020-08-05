package com.aleksanderkapera.covidstats.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aleksanderkapera.covidstats.repository.StatsRepository

/**
 * Factory for creating a [MainFragmentViewModel] with a constructor that takes a
 * [StatsRepository].
 */
class MainFragmentViewModelFactory(private val repository: StatsRepository) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainFragmentViewModel(repository) as T
    }
}