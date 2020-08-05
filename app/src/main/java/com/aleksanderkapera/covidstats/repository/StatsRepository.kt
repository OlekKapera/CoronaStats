package com.aleksanderkapera.covidstats.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.aleksanderkapera.covidstats.R
import com.aleksanderkapera.covidstats.domain.AllStatusStatistic
import com.aleksanderkapera.covidstats.domain.Country
import com.aleksanderkapera.covidstats.network.AllStatusStatisticNetwork
import com.aleksanderkapera.covidstats.network.CovidService
import com.aleksanderkapera.covidstats.network.asDatabaseModel
import com.aleksanderkapera.covidstats.network.asDomainModel
import com.aleksanderkapera.covidstats.room.StatsDatabase
import com.aleksanderkapera.covidstats.room.asDomainModel
import com.aleksanderkapera.covidstats.util.DateConverter
import com.aleksanderkapera.covidstats.util.SharedPrefsManager
import com.aleksanderkapera.covidstats.util.asString
import com.aleksanderkapera.covidstats.util.replaceZoneString
import kotlinx.coroutines.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

class StatsRepository private constructor(private val database: StatsDatabase) {

    companion object {
        // For Singleton instantiation
        @Volatile
        private var instance: StatsRepository? = null

        fun getInstance(database: StatsDatabase) =
            instance ?: synchronized(this) {
                instance ?: StatsRepository(database).also { instance = it }
            }
    }

    private val viewModelJob = SupervisorJob()
    private val scope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val stats: LiveData<List<AllStatusStatistic>> =
        Transformations.map(
            database.statsDao().getLatestStats(
                SharedPrefsManager.getList<Country>(R.string.prefs_chosen_countries.asString())
                    ?.get(0)?.iso2
            )
        ) { it.asDomainModel(database) }

    var countries: LiveData<List<Country>> =
        Transformations.map(database.countriesDao().getCountries()) {
            it.asDomainModel()
        }

    private val _todayStats = MutableLiveData<MutableList<LiveData<AllStatusStatistic>?>>()

    val todayStats: LiveData<MutableList<LiveData<AllStatusStatistic>?>>
        get() = _todayStats

    /**
     * Based on stats accessible in database fetches all accessible ones from the api
     */
    suspend fun updateStats() {
        withContext(Dispatchers.IO) {
            val userCountries =
                SharedPrefsManager.getList<Country>(R.string.prefs_chosen_countries.asString())

            // latest database update time
            val latestUpdateTime =
                SharedPrefsManager.get<Long>(R.string.prefs_last_fetched_date.asString())

            if (latestUpdateTime == null)
            // table has no entries, fetch all data
                getStats(userCountries)
            else {
                val updateDate = DateTime(latestUpdateTime).plusHours(1)
                val lastPossibleDate =
                    DateTime.now().minusDays(1).withZone(DateTimeZone.UTC)
                        .withMillisOfDay(0)

                if (updateDate.isBefore(lastPossibleDate))
                // fetch only data from last one in DB to today's
                    getStatsByTime(
                        userCountries ?: throw Exception("No user countries provided!"),
                        updateDate,
                        lastPossibleDate
                    )
            }
        }
    }

    /**
     * Retrieves all stats from countries from day one
     */
    suspend fun getStats(countries: List<Country>?) {
        withContext(NonCancellable) {
            val deferredStats: MutableList<Deferred<List<AllStatusStatisticNetwork>>> =
                mutableListOf()
            val newStats: List<List<AllStatusStatisticNetwork>>

            countries?.forEach { country ->
                deferredStats.add(CovidService.service.getDayOneAllStatus(country.slug))
            }

            newStats = deferredStats.awaitAll()
            database.statsDao().deleteAllStats()
            newStats.forEach { statistic ->
                database.statsDao().insertStatistic(*statistic.asDatabaseModel())
            }

            updateTodayStats(countries)
        }
    }

    /**
     * Retrieve countries from API and update database
     */
    suspend fun updateCountries() {
        if (countries.value.isNullOrEmpty()) {
            withContext(Dispatchers.IO) {
                val newCountries = CovidService.service.getAllCountries().await()
                database.countriesDao().insertCountries(*newCountries.asDatabaseModel())
            }
        }
    }

    /**
     * Retrieves overall statistics reported in [country] from yesterday's stats
     */
    suspend fun getStatsByTime(
        countries: List<Country>,
        from: DateTime,
        to: DateTime
    ) {
        val deferredStats: MutableList<Deferred<List<AllStatusStatisticNetwork>>> =
            mutableListOf()
        var newStats: List<List<AllStatusStatisticNetwork>>?
        withContext(Dispatchers.IO) {
            val fromUtc =
                from.toDateTime(DateTimeZone.UTC).toString(DateConverter).replaceZoneString()
            val toUtc = to.toDateTime(DateTimeZone.UTC).toString(DateConverter).replaceZoneString()

            countries.forEach { country ->
                deferredStats.add(
                    CovidService.service.getStatsByTime(
                        country.slug,
                        fromUtc,
                        toUtc
                    )
                )
            }
            newStats = deferredStats.awaitAll()
            newStats?.let { newStats ->
                newStats.forEach { newStats ->
                    val newStatsDatabase = newStats.asDatabaseModel()
                    database.statsDao().insertStatistic(*newStatsDatabase)
                }

                updateTodayStats(countries)

                SharedPrefsManager.put<Long>(
                    newStats.last().last().asDomainModel(database).date.millis,
                    R.string.prefs_last_fetched_date.asString()
                )
            }
        }
    }

    /**
     * Gets stats from last [numberOfDays] days
     */
    suspend fun getStatsFromLastDays(
        countries: List<Country>,
        numberOfDays: Int
    ) {
        val now = DateTime.now().withHourOfDay(2).withMinuteOfHour(0).withSecondOfMinute(0)
            .withMillisOfSecond(0)
        val from = now.minusDays(numberOfDays)

        getStatsByTime(countries, from, now)
    }

    /**
     * Updates latest stats
     */
    fun updateTodayStats(countries: List<Country>?) {
        countries?.let {
            _todayStats.value = mutableListOf()

            countries.forEach { country ->
                val lastStats = database.statsDao().getLastStats(country.iso2, 2)?.map {
                    it.asDomainModel(database)
                }?.toMutableList() ?: mutableListOf()

                if (lastStats.size == 2) {
                    // update today's net difference stats
                    lastStats[0].confirmed -= lastStats[1].confirmed
                    lastStats[0].active -= lastStats[1].active
                    lastStats[0].deaths -= lastStats[1].deaths
                    lastStats[0].recovered -= lastStats[1].recovered
                }
                _todayStats.value?.add(MutableLiveData(lastStats[0]))
            }
        }
    }

    fun getCountriesByName(countryName: String): List<Country>? =
        database.countriesDao().getCountryByName(countryName)?.map { it.asDomainModel() }
}