package com.aleksanderkapera.covidstats.domain

import org.joda.time.DateTime

data class AllStatusStatistic(
    val country: Country,
    val province: String,
    val city: String,
    val cityCode: String,
    val latitude: String,
    val longitude: String,
    var confirmed: Long,
    var deaths: Long,
    var recovered: Long,
    var active: Long,
    val date: DateTime
)