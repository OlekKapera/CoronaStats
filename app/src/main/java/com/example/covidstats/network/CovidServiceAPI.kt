package com.example.covidstats.network

import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CovidServiceAPI {
    @GET("dayone/country/{country}/status/{status}")
    fun getDayOneByStatus(
        @Path("country") country: String,
        @Path("status") status: String
    ): Deferred<List<StatisticNetwork>>

    @GET("country/{country}")
    fun getStatsByTime(
        @Path("country") country: String,
        @Query("from") fromDate: String,
        @Query("to") toDate: String
    ): Deferred<List<StatisticNetwork>>

    @GET("countries")
    fun getAllCountries(): Deferred<List<CountryNetwork>>
}