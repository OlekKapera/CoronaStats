package com.aleksanderkapera.covidstats.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aleksanderkapera.covidstats.R
import com.aleksanderkapera.covidstats.domain.AllStatusStatistic
import com.aleksanderkapera.covidstats.domain.Country
import com.aleksanderkapera.covidstats.repository.StatsRepository
import com.aleksanderkapera.covidstats.ui.MainFragment
import com.aleksanderkapera.covidstats.util.SharedPrefsManager
import com.aleksanderkapera.covidstats.util.asString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


/**
 * ViewModel for [MainFragment]
 */
class MainFragmentViewModel(private val repository: StatsRepository) : ViewModel() {

    private val viewModelJob = SupervisorJob()
    private val scope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val userCountries =
        SharedPrefsManager.getList<Country>(R.string.prefs_chosen_countries.asString())

    private val _exceptionCaughtEvent = MutableLiveData<Boolean>()
    val exceptionCaughtEvent: LiveData<Boolean>
        get() = _exceptionCaughtEvent

    val statistics = repository.stats

    val countries = repository.countries
    val todayStats: LiveData<MutableList<LiveData<AllStatusStatistic>?>> = repository.todayStats

    val hintCountries = MutableLiveData<List<Country>?>()

    init {
        scope.launch {
            try {
                repository.updateTodayStats(userCountries)
                repository.updateCountries()
                repository.updateStats()
            } catch (e: Exception) {
                engageExceptionAction()
                Log.e(this.javaClass.simpleName, e.message ?: "")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
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
}