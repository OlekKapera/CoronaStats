package com.example.covidstats.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.covidstats.domain.StatusEnum

@Dao
interface StatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStatistic(vararg stats: StatusTable)

    @Query("select * from statustable where countryCode in (:countryCodes) and status in (:statuses)")
    fun getStatisticByCountry(
        countryCodes: List<String>? = listOf("US"),
        statuses: List<String>? = listOf(StatusEnum.CONFIRMED.value)
    ): LiveData<List<StatusTable>>

    @Query("delete from statustable")
    fun deleteAllStats()
}