package com.aleksanderkapera.covidstats.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface StatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStatistic(vararg stats: AllStatusStatisticTable)

    @Query("select * from allstatusstatistictable where countryCode in (:countryCodes)")
    fun getStatisticByCountry(countryCodes: List<String>? = listOf("US")): LiveData<List<AllStatusStatisticTable>>

    @Query("delete from allstatusstatistictable")
    fun deleteAllStats()

    @Query("select * from allstatusstatistictable where countryCode = :countryCode order by date desc")
    fun getLatestStats(countryCode: String? = "US"): LiveData<List<AllStatusStatisticTable>>
}