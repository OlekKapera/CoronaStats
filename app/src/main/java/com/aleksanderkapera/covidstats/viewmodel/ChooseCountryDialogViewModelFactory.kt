package com.aleksanderkapera.covidstats.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aleksanderkapera.covidstats.repository.StatsRepository

/**
 * Factory for creating a [ChooseCountryDialogViewModel] with a constructor that takes a
 * [StatsRepository].
 */
class ChooseCountryDialogViewModelFactory(private val repository: StatsRepository) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ChooseCountryDialogViewModel(repository) as T
    }
}