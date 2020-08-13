package com.aleksanderkapera.covidstats.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aleksanderkapera.covidstats.domain.Country
import com.aleksanderkapera.covidstats.repository.StatsRepository

class ChooseCountryDialogViewModel(private val repository: StatsRepository) : ViewModel() {

    val hintCountries = MutableLiveData<List<Country>?>()

    private val _onPositiveButtonClickEvent = MutableLiveData<Boolean>()
    val onPositiveButtonClickEvent: LiveData<Boolean>
        get() = _onPositiveButtonClickEvent


    fun getCountriesByName(countryName: String) {
        hintCountries.value = repository.getCountriesByName(countryName)
    }

    fun onPositiveButtonClick() {
        _onPositiveButtonClickEvent.value = true
    }

    fun finishOnPositiveButtonClick() {
        _onPositiveButtonClickEvent.value = false
    }
}