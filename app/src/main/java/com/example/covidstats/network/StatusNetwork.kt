package com.example.covidstats.network

import com.example.covidstats.domain.Status
import com.example.covidstats.domain.StatusEnum
import com.example.covidstats.room.StatusTable
import com.example.covidstats.util.DateConverter
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

/**
 * Used for communication with API. Holds retrieved statistics.
 */
@JsonClass(generateAdapter = true)
data class StatusNetwork(
    @Json(name = "Country") val countryName: String,
    @Json(name = "CountryCode") val countryCode: String,
    @Json(name = "Lat") val latitude: String,
    @Json(name = "Lon") val longitude: String,
    @Json(name = "Cases") val cases: Long,
    @Json(name = "Status") val status: String,
    @Json(name = "Date") val date: String
)

/**
 * Converts [StatusNetwork] to [Status]
 */
fun List<StatusNetwork>.asDomainModel(): List<Status> {
    return map {
        Status(
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

fun List<StatusNetwork>.asDatabaseModel(): Array<StatusTable> {
    return map {
        StatusTable(
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