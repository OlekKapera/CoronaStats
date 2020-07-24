package com.example.covidstats.domain

import org.joda.time.DateTime

/**
 * Data class representing statistics.
 */
data class Statistic(
    val countryName: String,
    val countryCode: String,
    val latitude: String,
    val longitude: String,
    val cases: Long,
    val status: StatusEnum,
    val date: DateTime
) {
    override fun toString(): String {
        return "countryName = $countryName | countryCode = $countryCode | lat = $latitude | long = $longitude | cases = $cases | status = ${status.value} | date = ${date.toString()}\n\n"
    }
}

enum class StatusEnum(val value: String) {
    RECOVERED("recovered"),
    CONFIRMED("confirmed"),
    DEATHS("deaths")
}