package com.aleksanderkapera.covidstats.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.aleksanderkapera.covidstats.R
import com.aleksanderkapera.covidstats.domain.Country
import com.aleksanderkapera.covidstats.repository.StatsRepository
import com.aleksanderkapera.covidstats.room.asDomainModel
import com.aleksanderkapera.covidstats.room.getDatabase
import com.aleksanderkapera.covidstats.ui.MainFragment
import com.aleksanderkapera.covidstats.util.DateStandardConverter
import com.aleksanderkapera.covidstats.util.SharedPrefsManager
import com.aleksanderkapera.covidstats.util.asString
import kotlinx.coroutines.*


/**
 * ViewModel for [MainFragment]
 */
class MainFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val viewModelJob = SupervisorJob()
    private val scope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val database = getDatabase(application)
    private val repository = StatsRepository(database)

    private val userCountries =
        SharedPrefsManager.getList<Country>(R.string.prefs_chosen_countries.asString())

    private val _exceptionCaughtEvent = MutableLiveData<Boolean>()
    val exceptionCaughtEvent: LiveData<Boolean>
        get() = _exceptionCaughtEvent

    val statistics = repository.stats

    val countries = repository.countries
    val todayStats = repository.todayStats

    val hintCountries = MutableLiveData<List<Country>?>()

    init {
        scope.launch {
            try {
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
        hintCountries.value =
            database.countriesDao.getCountryByName(countryName)?.map { it.asDomainModel() }
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

    class MainFragmentViewModelFactory(private val application: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainFragmentViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainFragmentViewModel(application) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}