package com.aleksanderkapera.covidstats.network

import com.aleksanderkapera.covidstats.domain.Statistic
import com.aleksanderkapera.covidstats.domain.StatusEnum
import com.aleksanderkapera.covidstats.room.StatisticTable
import com.aleksanderkapera.covidstats.util.DateConverter
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.joda.time.DateTime

/**
 * Used for communication with API. Holds retrieved statistics.
 */
@JsonClass(generateAdapter = true)
data class StatisticNetwork(
    @Json(name = "Country") val countryName: String,
    @Json(name = "CountryCode") val countryCode: String,
    @Json(name = "Lat") val latitude: String,
    @Json(name = "Lon") val longitude: String,
    @Json(name = "Cases") val cases: Long,
    @Json(name = "Status") val status: String,
    @Json(name = "Date") val date: String
)

/**
 * Converts [StatisticNetwork] to [Statistic]
 */
fun List<StatisticNetwork>.asDomainModel(): List<Statistic> {
    return map {
        Statistic(
            countryName = it.countryName,
            countryCode = it.countryCode,
            latitude = it.latitude,
            longitude = it.longitude,
            cases = it.cases,
            status = StatusEnum.valueOf(it.status.toUpperCase()),
            date = DateTime.parse(it.date, DateConverter)
        )
    }
}

fun List<StatisticNetwork>.asDatabaseModel(): Array<StatisticTable> {
    return map {
        StatisticTable(
            countryName = it.countryName,
            countryCode = it.countryCode,
            latitude = it.latitude,
            longitude = it.longitude,
            cases = it.cases,
            status = it.status,
            date = DateTime.parse(it.date, DateConverter).millis
        )
    }.toTypedArray()
}