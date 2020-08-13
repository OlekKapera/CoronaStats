package com.aleksanderkapera.covidstats.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aleksanderkapera.covidstats.R
import com.aleksanderkapera.covidstats.domain.AllStatusStatistic
import com.aleksanderkapera.covidstats.domain.Country
import com.aleksanderkapera.covidstats.domain.asDatabaseModel
import com.aleksanderkapera.covidstats.repository.StatsRepository
import com.aleksanderkapera.covidstats.room.AllStatusStatisticTable
import com.aleksanderkapera.covidstats.ui.LatestStatsWidget.Companion.sendRefreshWidgetIntent
import com.aleksanderkapera.covidstats.ui.MainFragment
import com.aleksanderkapera.covidstats.util.*
import kotlinx.coroutines.launch


/**
 * ViewModel for [MainFragment]
 */
class MainFragmentViewModel(private val repository: StatsRepository) : ViewModel() {

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

    private val _todayStats = MutableLiveData<MutableList<LiveData<AllStatusStatistic>?>>()
    val todayStats: LiveData<MutableList<LiveData<AllStatusStatistic>?>>
        get() = _todayStats

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

    /**
     * Updates shared preferences with newest updated date of fetched statistics
     */
    fun updateLastFetchedDate() {
        _todayStats.value?.forEach { liveStats ->
            liveStats?.value?.let { stats ->
                lastSavedDate.find { it.countrySlug == stats.country.slug }.also { pref ->
                    if (pref != null)
                        pref.date = stats.date.millis
                    else
                        lastSavedDate.add(
                            DateLastSavedStatsModel(
                                stats.country.slug,
                                stats.date.millis
                            )
                        )
                }
            }
        }

        SharedPrefsManager.putList<DateLastSavedStatsModel>(
            lastSavedDate,
            R.string.prefs_last_fetched_date.asString()
        )
    }

    /**
     * Updates latest stats
     */
    fun updateTodayStats() {
        userCountries.value?.let { countries ->
            _todayStats.value = mutableListOf()

            viewModelScope.launch {
                val statsList = mutableListOf<LiveData<AllStatusStatistic>?>()
                countries.forEach { country ->
                    val dates =
                        mutableListOf(
                            lastSavedDate.find { it.countrySlug == country.slug }?.date ?: 0L
                        )
                    dates.add(dates[0].minus(DAY_IN_MILLIS))

                    val lastStats = repository.getLastStatsCombined(
                        country
                    )

                    if (lastStats.size != 2) {
                        //TODO fetch stats from API
                    } else {
                        // update today's net difference stats
                        lastStats[0].confirmed -= lastStats[1].confirmed
                        lastStats[0].active -= lastStats[1].active
                        lastStats[0].deaths -= lastStats[1].deaths
                        lastStats[0].recovered -= lastStats[1].recovered

                        statsList.add(MutableLiveData(lastStats[0]))
                    }
                }
                _todayStats.value = statsList

                SharedPrefsManager.putList<AllStatusStatisticTable?>(
                    statsList.map { it?.value?.asDatabaseModel() },
                    R.string.prefs_latest_stats.asString()
                )

                sendRefreshWidgetIntent()
            }
        }
    }

    fun updateStats() = viewModelScope.launch {
        try {
            loadingStarted()
            repository.updateStats()
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