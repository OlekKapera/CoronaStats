package com.aleksanderkapera.covidstats.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aleksanderkapera.covidstats.domain.Country
import com.aleksanderkapera.covidstats.repository.StatsRepository
import kotlinx.coroutines.launch

class ChooseCountryDialogViewModel(private val repository: StatsRepository) : ViewModel() {

    val hintCountries = MutableLiveData<List<Country>?>()
    val countries = repository.countries
    val clickedCountries = MutableLiveData(mutableSetOf<Country>())

    private val _onPositiveButtonClickEvent = MutableLiveData<Boolean>()
    val onPositiveButtonClickEvent: LiveData<Boolean>
        get() = _onPositiveButtonClickEvent

    fun getCountriesByName(countryName: String) {
        hintCountries.value = repository.getCountriesByName(countryName)
    }

    fun updateStats() = viewModelScope.launch { repository.updateStats() }

    fun onPositiveButtonClick() {
        _onPositiveButtonClickEvent.value = true
    }

    fun finishOnPositiveButtonClick() {
        _onPositiveButtonClickEvent.value = false
    }
}