package com.aleksanderkapera.covidstats.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aleksanderkapera.covidstats.R
import com.aleksanderkapera.covidstats.domain.AllStatusStatistic
import com.aleksanderkapera.covidstats.domain.Country
import com.aleksanderkapera.covidstats.repository.StatsRepository
import com.aleksanderkapera.covidstats.ui.MainFragment
import com.aleksanderkapera.covidstats.util.SharedPrefsManager
import com.aleksanderkapera.covidstats.util.asString
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


/**
 * ViewModel for [MainFragment]
 */
class MainFragmentViewModel(private val repository: StatsRepository) : ViewModel() {

    val userCountries =
        SharedPrefsManager.getList<Country>(R.string.prefs_chosen_countries.asString())

    private val _exceptionCaughtEvent = MutableLiveData<Boolean>()
    val exceptionCaughtEvent: LiveData<Boolean>
        get() = _exceptionCaughtEvent

    val statistics = repository.stats

    val countries = repository.countries

    private val _todayStats = MutableLiveData<MutableList<LiveData<AllStatusStatistic>?>>()
    val todayStats: LiveData<MutableList<LiveData<AllStatusStatistic>?>>
        get() = _todayStats

    val hintCountries = MutableLiveData<List<Country>?>()

    init {
        viewModelScope.launch {
            try {
                repository.updateCountries()
                repository.updateStats()
            } catch (e: Exception) {
                engageExceptionAction()
                Log.e(this.javaClass.simpleName, e.message ?: "")
            }
        }
    }

    fun getCountriesByName(countryName: String) {
        hintCountries.value = repository.getCountriesByName(countryName)
    }

    /**
     * Is being called when network related exception is called and changed liveData's value for
     * further handling
     */
    private fun engageExceptionAction() {
        _exceptionCaughtEvent.value = true
    }

    /**
     * Resets exception event after it was handled
     */
    fun exceptionHandled() {
        _exceptionCaughtEvent.value = false
    }

    /**
     * Updates shared preferences with newest updated date of fetched statistics
     */
    fun updateLastFetchedDate() {
        if (!statistics.value.isNullOrEmpty()) {
            statistics.value?.first()?.date?.millis?.let { millis ->
                SharedPrefsManager.put<Long>(
                    millis,
                    R.string.prefs_last_fetched_date.asString()
                )
            }
        }
    }

    /**
     * Updates latest stats
     */
    fun updateTodayStats() {
        userCountries?.let { countries ->
            _todayStats.value = mutableListOf()

            viewModelScope.launch {
                countries.forEach { country ->
                    val lastStats = repository.getLastStats(country)

                    if (lastStats.size != 2) {
                        //TODO fetch stats from API
                    } else {
                        // update today's net difference stats
                        lastStats[0].confirmed -= lastStats[1].confirmed
                        lastStats[0].active -= lastStats[1].active
                        lastStats[0].deaths -= lastStats[1].deaths
                        lastStats[0].recovered -= lastStats[1].recovered

                        _todayStats.value?.add(MutableLiveData(lastStats[0]))
                    }
                }
            }
        }
    }
}