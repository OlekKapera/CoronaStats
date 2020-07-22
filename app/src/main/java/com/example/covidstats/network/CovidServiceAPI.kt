package com.example.covidstats.network

import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Path

interface CovidServiceAPI {
    @GET("dayone/country/{country}/status/{status}")
    fun getDayOneByStatus(
        @Path("country") country: String,
        @Path("status") status: String
    ): Deferred<List<StatusNetwork>>
}