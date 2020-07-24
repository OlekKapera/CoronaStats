package com.example.covidstats.network

import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CovidServiceAPI {
    @GET("/dayone/country/{country}")
    fun getDayOneAllStatus(@Path("country") country: String): Deferred<List<AllStatusStatisticNetwork>>

    @GET("/country/{country}")
    fun getStatsByTime(
        @Path("country") countrySlug: String,
        @Query("from") fromDate: String,
        @Query("to") toDate: String
    ): Deferred<List<AllStatusStatisticNetwork>>

    @GET("/countries")
    fun getAllCountries(): Deferred<List<CountryNetwork>>
}