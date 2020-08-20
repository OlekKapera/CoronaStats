package com.aleksanderkapera.covidstats.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.aleksanderkapera.covidstats.CovidStatsApp
import com.aleksanderkapera.covidstats.R
import com.aleksanderkapera.covidstats.domain.Country
import com.aleksanderkapera.covidstats.repository.StatsRepository
import com.aleksanderkapera.covidstats.room.AllStatusStatisticTable
import com.aleksanderkapera.covidstats.room.asDomainModel
import com.aleksanderkapera.covidstats.ui.MainFragment
import com.aleksanderkapera.covidstats.util.*
import kotlinx.coroutines.launch


/**
 * ViewModel for [MainFragment]
 */
class MainFragmentViewModel(private val repository: StatsRepository) : ViewModel() {

    private val database = InjectorUtils.getStatsDatabase(CovidStatsApp.context)

    val userCountries =
        LiveSharedPreferences.getObjectList<Country>(R.string.prefs_chosen_countries.asString())

    private val _exceptionCaughtEvent = MutableLiveData<Boolean>()
    val exceptionCaughtEvent: LiveData<Boolean>
        get() = _exceptionCaughtEvent

    private val _loadingEvent = MutableLiveData<Boolean>()
    val loadingEvent: LiveData<Boolean>
        get() = _loadingEvent

    val statistics = repository.stats
    val countries = repository.countries

    val todayStats =
        Transformations.map(LiveSharedPreferences.getObjectList<AllStatusStatisticTable>(R.string.prefs_latest_stats.asString())) {
            it?.asDomainModel(database)
        }

    private val lastSavedDate =
        SharedPrefsManager.getList<DateLastSavedStatsModel>(R.string.prefs_last_fetched_date.asString())
            ?.toMutableList() ?: mutableListOf()

    init {
        viewModelScope.launch {
            try {
                loadingStarted()
                repository.updateCountries()
                repository.updateStats()
            } catch (e: Throwable) {
                engageExceptionAction()
                Log.e(this.javaClass.simpleName, e.toString())
            } finally {
                loadingFinished()
            }
        }
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

    fun updateStats(clickedCountries: List<Country>? = null) = viewModelScope.launch {
        try {
            loadingStarted()
            repository.updateStats(clickedCountries)
        } catch (e: Throwable) {
            engageExceptionAction()
            Log.e(this.javaClass.simpleName, e.toString())
        } finally {
            loadingFinished()
        }
    }

    private fun loadingStarted() {
        _loadingEvent.value = true
    }

    private fun loadingFinished() {
        _loadingEvent.value = false
    }
}