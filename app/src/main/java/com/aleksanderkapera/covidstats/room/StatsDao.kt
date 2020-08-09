package com.aleksanderkapera.covidstats.room

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery

@Dao
interface StatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStatistic(vararg stats: AllStatusStatisticTable)

    @Query("select * from allstatusstatistictable where countryCode in (:countryCodes) order by date desc")
    fun getStatisticByCountry(countryCodes: List<String>? = listOf("US")): LiveData<List<AllStatusStatisticTable>>

    @Query("delete from allstatusstatistictable")
    fun deleteAllStats()

    @Query("select * from allstatusstatistictable where countryCode = :countryCode order by date desc")
    fun getLatestStats(countryCode: String? = "US"): LiveData<List<AllStatusStatisticTable>>

    @Query("select count(*) from allstatusstatistictable")
    fun getSizeOfStats(): Int

    @Query("select * from allstatusstatistictable where countryCode = :countryCode order by date desc limit :numberOfStats")
    fun getLastStats(
        countryCode: String? = "US",
        numberOfStats: Int
    ): List<AllStatusStatisticTable>?

    @Query("select date, countryName, countryCode, province, cityName, cityCode, latitude, longitude, sum(confirmed) as confirmed, sum(deaths) as deaths, sum(recovered) as recovered, sum(active) as active from allstatusstatistictable where countryCode = :countryCode group by date order by date desc limit :limit")
    fun getLastStatsCombined(
        countryCode: String? = "US",
        limit: Int = 2
    ): List<AllStatusStatisticTable>?
}