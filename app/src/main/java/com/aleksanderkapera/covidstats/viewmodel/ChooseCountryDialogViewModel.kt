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
import com.aleksanderkapera.covidstats.util.DateLastSavedStatsModel
import com.aleksanderkapera.covidstats.util.SharedPrefsManager
import com.aleksanderkapera.covidstats.util.asString
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

    /**
     * Fetch new country's statistics from API
     */
    suspend fun fetchNewCountry(latestStatsPreference: List<AllStatusStatistic>): AllStatusStatistic? {
        var todayStats: AllStatusStatistic? = null
        clickedCountries.value?.first()?.let { country ->
            val userCountries =
                SharedPrefsManager.getList<Country>(R.string.prefs_chosen_countries.asString())
                    ?: listOf()
            SharedPrefsManager.putList<Country>(
                listOf(country),
                R.string.prefs_chosen_countries.asString()
            )

            try {
                repository.updateStats()
            } catch (t: Throwable) {
                Log.e(ChooseCountryDialogViewModel::class.simpleName, t.toString())
            } finally {
                todayStats = updateTodayStats(country, latestStatsPreference.toMutableList())
                updateLastFetchedDate(todayStats)
                SharedPrefsManager.putList<Country>(
                    userCountries,
                    R.string.prefs_chosen_countries.asString()
                )
            }
        }
        return todayStats
    }

    /**
     * Saves today's stats to shared preferences
     */
    private fun updateTodayStats(
        country: Country,
        previousStats: MutableList<AllStatusStatistic>
    ): AllStatusStatistic {
        val lastStats = repository.getLastStatsCombined(
            country
        )

        if (lastStats.size == 2) {
            // update today's net difference stats
            lastStats[0].confirmed -= lastStats[1].confirmed
            lastStats[0].active -= lastStats[1].active
            lastStats[0].deaths -= lastStats[1].deaths
            lastStats[0].recovered -= lastStats[1].recovered

            previousStats.add(lastStats[0])

            SharedPrefsManager.putList<AllStatusStatisticTable?>(
                previousStats.map { it.asDatabaseModel() },
                R.string.prefs_latest_stats.asString()
            )
        }

        return previousStats.last()
    }

    /**
     * Updates shared preferences regarding last fetched date
     */
    private fun updateLastFetchedDate(todayStats: AllStatusStatistic?) {
        todayStats?.let { stats ->
            val previousDates =
                SharedPrefsManager.getList<DateLastSavedStatsModel>(R.string.prefs_last_fetched_date.asString())
                    ?.toMutableList() ?: mutableListOf()
            previousDates.removeIf { it.countrySlug == todayStats.country.slug }
            previousDates.add(DateLastSavedStatsModel(stats.country.slug, stats.date.millis))

            SharedPrefsManager.putList<DateLastSavedStatsModel>(
                previousDates,
                R.string.prefs_last_fetched_date.asString()
            )
        }
    }

    fun onPositiveButtonClick() {
        _onPositiveButtonClickEvent.value = true
    }

    fun finishOnPositiveButtonClick() {
        _onPositiveButtonClickEvent.value = false
    }
}