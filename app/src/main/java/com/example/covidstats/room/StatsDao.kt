package com.example.covidstats.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.covidstats.domain.StatusEnum

@Dao
interface StatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStatistic(vararg stats: StatisticTable)

    @Query("select * from statistictable where countryCode in (:countryCodes) and status in (:statuses)")
    fun getStatisticByCountry(
        countryCodes: List<String>? = listOf("US"),
        statuses: List<String>? = listOf(StatusEnum.CONFIRMED.value)
    ): LiveData<List<StatisticTable>>

    @Query("delete from statistictable")
    fun deleteAllStats()

    @Query("select * from statistictable where countryCode = :countryCode order by date desc")
    fun getLatestStats(countryCode: String? = "US"): LiveData<List<StatisticTable>>
}