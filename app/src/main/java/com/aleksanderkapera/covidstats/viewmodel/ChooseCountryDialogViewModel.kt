package com.aleksanderkapera.covidstats.viewmodel

import androidx.lifecycle.*
import com.aleksanderkapera.covidstats.CovidStatsApp
import com.aleksanderkapera.covidstats.R
import com.aleksanderkapera.covidstats.domain.Country
import com.aleksanderkapera.covidstats.repository.StatsRepository
import com.aleksanderkapera.covidstats.util.LiveSharedPreferences
import com.aleksanderkapera.covidstats.util.asString
import kotlinx.coroutines.launch

class ChooseCountryDialogViewModel(private val repository: StatsRepository) : ViewModel() {

    val hintCountries = MutableLiveData<List<Country>?>()
    val userCountries =
        LiveSharedPreferences.getObjectList<Country>(R.string.prefs_chosen_countries.asString())

    private val _onPositiveButtonClickEvent = MutableLiveData<Boolean>()
    val onPositiveButtonClickEvent: LiveData<Boolean>
        get() = _onPositiveButtonClickEvent

    init {
        repository.countries
    }


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