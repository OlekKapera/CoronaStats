package com.example.covidstats.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.covidstats.domain.Status
import com.example.covidstats.domain.StatusEnum
import com.example.covidstats.network.CovidService
import com.example.covidstats.network.StatusNetwork
import com.example.covidstats.network.asDatabaseModel
import com.example.covidstats.room.StatsDatabase
import com.example.covidstats.room.asDomainModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class StatsRepository(private val database: StatsDatabase) {

    val stats: LiveData<List<Status>> =
        Transformations.map(database.statsDao.getStatisticByCountry()) {
            it.asDomainModel()
        }

    suspend fun getStats(countryCodes: List<String>, statuses: List<StatusEnum>) {
        withContext(Dispatchers.IO) {
            val deferredStats: MutableList<Deferred<List<StatusNetwork>>> = mutableListOf()
            var newStats: List<List<StatusNetwork>> = mutableListOf()

            countryCodes.forEach { code ->
                statuses.forEach { status ->
                    deferredStats.add(CovidService.service.getDayOneByStatus(code, status.value))
                }
            }
            newStats = deferredStats.awaitAll()
            database.statsDao.deleteAllStats()
            newStats.forEach { statistic ->
                database.statsDao.insertStatistic(*statistic.asDatabaseModel())
            }
        }
    }
}