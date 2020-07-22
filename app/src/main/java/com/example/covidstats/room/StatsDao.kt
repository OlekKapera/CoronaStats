package com.example.covidstats.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.covidstats.domain.StatusEnum

@Dao
interface StatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStatus(vararg stats: StatusTable)

    @Query("select * from statustable where countryCode = :countryCode and status = :status")
    fun getStatusByCountry(
        countryCode: String,
        status: String? = StatusEnum.CONFIRMED.value
    ): LiveData<List<StatusTable>>
}