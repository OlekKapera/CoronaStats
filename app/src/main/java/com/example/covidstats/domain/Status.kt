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
) {
    override fun toString(): String {
        return """countryName = $countryName\tcountryCode = $countryCode\tlat = $latitude\t
long = $longitude\tcases = $cases\tstatus = ${status.value}\tdate = ${date.toString()}\n\n"""
    }
}

enum class StatusEnum(val value: String) {
    RECOVERED("recovered"),
    CONFIRMED("confirmed"),
    DEATHS("deaths")
}