package com.example.covidstats.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.covidstats.domain.Statistic
import com.example.covidstats.domain.StatusEnum
import com.example.covidstats.network.CovidService
import com.example.covidstats.network.StatisticNetwork
import com.example.covidstats.network.asDatabaseModel
import com.example.covidstats.room.StatsDatabase
import com.example.covidstats.room.asDomainModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class StatsRepository(private val database: StatsDatabase) {

    val stats: LiveData<List<Statistic>> =
        Transformations.map(database.statsDao.getStatisticByCountry()) {
            it.asDomainModel()
        }

    suspend fun getStats(
        countryCodes: List<String>? = listOf("US"),
        statuses: List<StatusEnum>? = listOf(StatusEnum.CONFIRMED)
    ) {
        withContext(Dispatchers.IO) {
            val deferredStats: MutableList<Deferred<List<StatisticNetwork>>> = mutableListOf()
            var newStats: List<List<StatisticNetwork>> = mutableListOf()

            countryCodes?.forEach { code ->
                statuses?.forEach { status ->
                    deferredStats.add(CovidService.service.getDayOneByStatus(code, status.value))
                }
            }
            newStats = deferredStats.awaitAll()
            database.statsDao.deleteAllStats()
            newStats.forEach { statistic ->
                //TODO delete sublist
                database.statsDao.insertStatistic(*statistic.subList(0,1000).asDatabaseModel())
            }
        }
    }
}