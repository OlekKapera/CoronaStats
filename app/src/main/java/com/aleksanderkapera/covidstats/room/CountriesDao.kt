package com.aleksanderkapera.covidstats.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CountriesDao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCountries(vararg country: CountryTable)

    @Query("select * from countrytable")
    fun getCountries(): LiveData<List<CountryTable>>

    @Query("select * from countrytable where iso2 in (:iso)")
    fun getCountryByIso(iso: String): LiveData<CountryTable>
}