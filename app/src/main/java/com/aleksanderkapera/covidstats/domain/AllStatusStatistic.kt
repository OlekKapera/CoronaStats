package com.aleksanderkapera.covidstats.domain

import org.joda.time.DateTime

data class AllStatusStatistic(
    val country: Country,
    val province: String,
    val city: String,
    val cityCode: String,
    val latitude: String,
    val longitude: String,
    val confirmed: Long,
    val deaths: Long,
    val recovered: Long,
    val active: Long,
    val date: DateTime
)