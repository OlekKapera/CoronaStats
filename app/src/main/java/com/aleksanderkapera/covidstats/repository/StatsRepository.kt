package com.aleksanderkapera.covidstats.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.aleksanderkapera.covidstats.R
import com.aleksanderkapera.covidstats.domain.AllStatusStatistic
import com.aleksanderkapera.covidstats.domain.Country
import com.aleksanderkapera.covidstats.network.AllStatusStatisticNetwork
import com.aleksanderkapera.covidstats.network.CovidService
import com.aleksanderkapera.covidstats.network.asDatabaseModel
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

    /**
     * Based on stats accessible in database fetches all accessible ones from the api
     */
    suspend fun updateStats() {
        withContext(Dispatchers.IO) {
            val userCountries =
                SharedPrefsManager.getList<Country>(R.string.prefs_chosen_countries.asString())

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
        }
    }

    /**
     * Retrieves all stats from countries from day one
     */
    suspend fun getStats(countries: List<Country>?) {
        withContext(Dispatchers.IO) {
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
}