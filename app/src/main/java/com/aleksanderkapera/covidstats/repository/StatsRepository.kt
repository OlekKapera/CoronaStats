package com.aleksanderkapera.covidstats.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.aleksanderkapera.covidstats.R
import com.aleksanderkapera.covidstats.domain.AllStatusStatistic
import com.aleksanderkapera.covidstats.domain.Country
import com.aleksanderkapera.covidstats.domain.asDatabaseModel
import com.aleksanderkapera.covidstats.network.AllStatusStatisticNetwork
import com.aleksanderkapera.covidstats.network.CovidService
import com.aleksanderkapera.covidstats.network.asDatabaseModel
import com.aleksanderkapera.covidstats.room.AllStatusStatisticTable
import com.aleksanderkapera.covidstats.room.StatsDatabase
import com.aleksanderkapera.covidstats.room.asDomainModel
import com.aleksanderkapera.covidstats.util.*
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

    /**
     * Based on stats accessible in database fetches all accessible ones from the api
     */
    suspend fun updateStats(countriesToFetch: List<Country>? = null) {
        withContext(Dispatchers.IO) {
            val userCountries =
                countriesToFetch
                    ?: SharedPrefsManager.getList<Country>(R.string.prefs_chosen_countries.asString())

            // latest database update time
            val latestUpdateTime =
                SharedPrefsManager.getList<DateLastSavedStatsModel>(R.string.prefs_last_fetched_date.asString())

            val countriesNotFetched = mutableListOf<String>()
            latestUpdateTime?.let { latestUpdateTime ->
                userCountries?.forEach { country ->
                    if (latestUpdateTime.find { it.countrySlug == country.slug } == null) {
                        countriesNotFetched.add(country.slug)
                    }
                }
            }

            try {
                if (latestUpdateTime.isNullOrEmpty())
                // table has no entries, fetch all data
                    getStats(userCountries)
                else if (countriesNotFetched.isNotEmpty())
                // table doesn't contain only some country's data
                    getStats(userCountries?.filter { countriesNotFetched.contains(it.slug) })
                else {
                    latestUpdateTime.forEach loop@{ pair ->
                        val updateDate = DateTime(pair.date).plusHours(1)
                        val lastPossibleDate =
                            DateTime.now().minusDays(1).withZone(DateTimeZone.UTC)
                                .withMillisOfDay(0)

                        if (updateDate.isBefore(lastPossibleDate)) {
                            // fetch only data from last one in DB to today's
                            getStatsByTime(
                                userCountries
                                    ?: throw Exception("No user countries provided!"),
                                updateDate,
                                lastPossibleDate
                            )
                            return@loop
                        }
                    }

                }
            } finally {
                if (userCountries != null) {
                    val todayStats = updateTodayStats(userCountries)
                    updateLastFetchedDate(todayStats)
                }
            }
        }
    }

    /**
     * Retrieves all stats from countries from day one
     */
    suspend fun getStats(countries: List<Country>?) {
        withContext(Dispatchers.Unconfined + NonCancellable) {
            val newStats: List<List<AllStatusStatisticNetwork>>?
            val deferredStats: MutableList<Deferred<List<AllStatusStatisticNetwork>>> =
                mutableListOf()

            countries?.forEach { country ->
                deferredStats.add(CovidService.service.getDayOneAllStatus(country.slug))
            }

            newStats = deferredStats.awaitAll()
            newStats.forEach { statistic ->
                database.statsDao().insertStatistic(*statistic.asDatabaseModel())
            }
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
        var newStats: List<List<AllStatusStatisticNetwork>>? = emptyList()
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

        withContext(Dispatchers.IO) {
            newStats = deferredStats.awaitAll()
            newStats?.let { newStats ->
                newStats.forEach { newStats ->
                    val newStatsDatabase = newStats.asDatabaseModel()
                    database.statsDao().insertStatistic(*newStatsDatabase)
                }
//                updateLastFetchedDate(newStats)
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

    fun getCountriesByName(countryName: String): List<Country>? =
        database.countriesDao().getCountryByName(countryName)?.map { it.asDomainModel() }

    /**
     * Returns last [numberOfStats] from database of a given [country]
     */
    fun getLastStats(
        country: Country,
        numberOfStats: Int = 2
    ): MutableList<AllStatusStatistic> {
        return database.statsDao().getLastStats(country.iso2, numberOfStats)?.map {
            it.asDomainModel(database)
        }?.toMutableList() ?: mutableListOf()
    }

    /**
     * Returns last stats from database of a given [country] a combines numbers from all provinces
     */
    fun getLastStatsCombined(
        country: Country
    ): MutableList<AllStatusStatistic> {
        return database.statsDao().getLastStatsCombined(country.iso2)?.map {
            it.asDomainModel(database)
        }?.toMutableList() ?: mutableListOf()
    }

    /**
     * Saves today's stats to shared preferences
     */
    private fun updateTodayStats(
        countries: List<Country>
    ): List<AllStatusStatistic> {
        val previousStats =
            SharedPrefsManager.getList<AllStatusStatisticTable>(R.string.prefs_latest_stats.asString())
                ?.map { it.asDomainModel(database) }?.toMutableList() ?: mutableListOf()

        countries.forEach { country ->
            val lastStats = getLastStatsCombined(
                country
            )

            if (lastStats.size == 2) {
                // update today's net difference stats
                lastStats[0].confirmed -= lastStats[1].confirmed
                lastStats[0].active -= lastStats[1].active
                lastStats[0].deaths -= lastStats[1].deaths
                lastStats[0].recovered -= lastStats[1].recovered

                previousStats.removeIf { it.country.iso2 == country.iso2 }
                previousStats.add(lastStats[0])
            }
        }

        SharedPrefsManager.putList<AllStatusStatisticTable?>(
            previousStats.map { it.asDatabaseModel() },
            R.string.prefs_latest_stats.asString()
        )

        return previousStats
    }

    /**
     * Updates shared preferences regarding last fetched date
     */
    private fun updateLastFetchedDate(todayStats: List<AllStatusStatistic>) {
        val lastDates =
            SharedPrefsManager.getList<DateLastSavedStatsModel>(R.string.prefs_last_fetched_date.asString())
                ?.toMutableList() ?: mutableListOf()
        todayStats.forEach { statistic ->
            lastDates.removeIf { it.countrySlug == statistic.country.slug }
            lastDates.add(DateLastSavedStatsModel(statistic.country.slug, statistic.date.millis))
        }

        lastDates.toList().let { dates ->
            SharedPrefsManager.putList<DateLastSavedStatsModel>(
                dates,
                R.string.prefs_last_fetched_date.asString()
            )
        }
    }
}