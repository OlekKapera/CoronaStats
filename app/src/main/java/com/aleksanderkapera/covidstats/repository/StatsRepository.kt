package com.aleksanderkapera.covidstats.repository

import androidx.lifecycle.*
import com.aleksanderkapera.covidstats.R
import com.aleksanderkapera.covidstats.domain.AllStatusStatistic
import com.aleksanderkapera.covidstats.domain.Country
import com.aleksanderkapera.covidstats.network.AllStatusStatisticNetwork
import com.aleksanderkapera.covidstats.network.CovidService
import com.aleksanderkapera.covidstats.network.asDatabaseModel
import com.aleksanderkapera.covidstats.network.asDomainModel
import com.aleksanderkapera.covidstats.room.CountryTable
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

    private val viewModelJob = SupervisorJob()
    private val scope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val stats: LiveData<List<AllStatusStatistic>> =
        Transformations.map(
            database.statsDao.getLatestStats(
                SharedPrefsManager.getList<Country>(R.string.prefs_chosen_countries.asString())
                    ?.get(0)?.iso2
            )
        ) { it.asDomainModel(database) }

    var countries: LiveData<List<Country>> =
        Transformations.map(database.countriesDao.getCountries()) {
            it.asDomainModel()
        }

    private val _todayStats: LiveData<AllStatusStatistic?> = Transformations.map(stats) { stats ->
        if (stats.size >= 2) {
            val lastStats = stats.subList(0, 2)

            if (lastStats.size == 2) {
                // update today's net difference stats
                lastStats[0].confirmed -= lastStats[1].confirmed
                lastStats[0].active -= lastStats[1].active
                lastStats[0].deaths -= lastStats[1].deaths
                lastStats[0].recovered -= lastStats[1].recovered
                return@map lastStats[0]
            }
        }
        null
    }

    val todayStats: LiveData<AllStatusStatistic?>
        get() = _todayStats

    /**
     * Based on stats accessible in database fetches all accessible ones from the api
     */
    suspend fun updateStats() {
        withContext(Dispatchers.IO) {
            val userCountries =
                SharedPrefsManager.getList<Country>(R.string.prefs_chosen_countries.asString())
            val userCountryCodes = userCountries?.map { it.iso2 } ?: emptyList()

            // latest database update time
            val latestUpdateTime =
                SharedPrefsManager.get<Long>(R.string.prefs_last_fetched_date.asString())

            if (latestUpdateTime == null)
            // table has no entries, fetch all data
                getStats(userCountryCodes)
            else {
                val updateDate = DateTime(latestUpdateTime).plusHours(1)
                val lastPossibleDate =
                    DateTime.now().minusDays(1).withZone(DateTimeZone.UTC)
                        .withMillisOfDay(0)

                if (updateDate.isBefore(lastPossibleDate))
                // fetch only data from last one in DB to today's
                    getStatsByTime(
                        userCountries?.get(0) ?: return@withContext,
                        updateDate,
                        lastPossibleDate
                    )
            }
        }
    }

    /**
     * Retrieves all stats from countries from day one
     */
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

            newStats?.let {
                SharedPrefsManager.put<Long>(
                    it.last().date.millis,
                    R.string.prefs_last_fetched_date.asString()
                )
            }

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

        getStatsByTime(country, from, now)
    }
}