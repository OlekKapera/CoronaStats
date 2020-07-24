package com.example.covidstats.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.covidstats.domain.AllStatusStatistic
import com.example.covidstats.domain.Country
import com.example.covidstats.network.AllStatusStatisticNetwork
import com.example.covidstats.network.CovidService
import com.example.covidstats.network.asDatabaseModel
import com.example.covidstats.room.StatsDatabase
import com.example.covidstats.room.asDomainModel
import com.example.covidstats.util.DateConverter
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.joda.time.DateTime

class StatsRepository(private val database: StatsDatabase) {

    val stats: LiveData<List<AllStatusStatistic>> =
        Transformations.map(database.statsDao.getStatisticByCountry()) {
            it.asDomainModel(database)
        }

    val countries: LiveData<List<Country>> =
        Transformations.map(database.countriesDao.getCountries()) {
            it.asDomainModel()
        }

    val todayStats: LiveData<List<AllStatusStatistic>> =
        Transformations.map(database.statsDao.getLatestStats()) {
            it.asDomainModel(database)
        }

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

    suspend fun updateTodayStats(country: Country) {
        withContext(Dispatchers.IO) {
            val now = DateTime.now()
            val newStats = CovidService.service.getStatsByTime(
                country.slug,
                now.minusDays(1).toString(DateConverter),
                now.toString(DateConverter)
            ).await()
            database.statsDao.insertStatistic(*newStats.asDatabaseModel())
        }
    }
}