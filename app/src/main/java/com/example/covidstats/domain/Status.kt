package com.example.covidstats.domain

import org.joda.time.DateTime

/**
 * Data class representing statistics.
 */
data class Status(
    val countryName: String,
    val countryCode: String,
    val latitude: String,
    val longitude: String,
    val cases: Long,
    val status: StatusEnum,
    val date: DateTime
)

enum class StatusEnum(val value: String) {
    RECOVERED("recovered"),
    CONFIRMED("confirmed"),
    DEATHS("deaths")
}