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

class StatsRepository(private val database: StatsDatabase) {

    val stats: LiveData<List<AllStatusStatistic>> =
        Transformations.map(
            database.statsDao.getLatestStats(
                SharedPrefsManager.getList<Country>(R.string.prefs_chosen_countries.asString())
                    ?.get(0)?.iso2
            )
        ) { it.asDomainModel(database) }

    val countries: LiveData<List<Country>> =
        Transformations.map(database.countriesDao.getCountries()) {
            it.asDomainModel()
        }

    private val _todayStats = MutableLiveData<AllStatusStatistic>()
    val todayStats: LiveData<AllStatusStatistic>
        get() = _todayStats

    suspend fun getStats(countryCodes: List<String>? = listOf("US")) {
        withContext(Dispatchers.IO) {
            val deferredStats: MutableList<Deferred<List<AllStatusStatisticNetwork>>> =
                mutableListOf()
            val newStats: List<List<AllStatusStatisticNetwork>>

            countryCodes?.forEach { code ->
                deferredStats.add(CovidService.service.getDayOneAllStatus(code))
            }

            newStats = deferredStats.awaitAll()
            database.statsDao.deleteAllStats()
            newStats.forEach { statistic ->
                database.statsDao.insertStatistic(*statistic.asDatabaseModel())
            }
        }
    }

    suspend fun updateCountries() {
        withContext(Dispatchers.IO) {
            val newCountries = CovidService.service.getAllCountries().await()
            database.countriesDao.insertCountries(*newCountries.asDatabaseModel())
        }
    }

    /**
     * Retrieves overall statistics reported in a [country] from yesterday's stats
     */
    suspend fun getStatsByTime(
        country: Country,
        from: DateTime,
        to: DateTime
    ): List<AllStatusStatistic>? {
        var newStats: List<AllStatusStatistic>? = null
        withContext(Dispatchers.IO) {
            val fromUtc =
                from.toDateTime(DateTimeZone.UTC).toString(DateConverter).replaceZoneString()
            val toUtc = to.toDateTime(DateTimeZone.UTC).toString(DateConverter).replaceZoneString()

            val newStatsNetwork = CovidService.service.getStatsByTime(
                country.slug,
                fromUtc,
                toUtc
            ).await()

            newStats = newStatsNetwork.asDomainModel(database)
            database.statsDao.insertStatistic(*newStatsNetwork.asDatabaseModel())

            return@withContext newStats
        }
        return newStats
    }

    /**
     * Gets stats from last [numberOfDays] days
     */
    suspend fun getStatsFromLastDays(
        country: Country,
        numberOfDays: Int
    ) {
        val now = DateTime.now().withHourOfDay(2).withMinuteOfHour(0).withSecondOfMinute(0)
            .withMillisOfSecond(0)
        val from = now.minusDays(numberOfDays)

        val newStats = getStatsByTime(country, from, now)

        newStats?.takeLast(2)?.let { lastStats ->

            // update today's net difference stats
            lastStats[1].confirmed -= lastStats[0].confirmed
            lastStats[1].active -= lastStats[0].active
            lastStats[1].deaths -= lastStats[0].deaths
            lastStats[1].recovered -= lastStats[0].recovered
            _todayStats.value = lastStats[1]
        }
    }
}