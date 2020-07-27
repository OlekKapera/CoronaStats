package com.aleksanderkapera.covidstats.util

import com.aleksanderkapera.covidstats.CovidStatsApp
import org.joda.time.format.DateTimeFormat

private val context = CovidStatsApp.context

// Converter used for converting API's date string to joda's DateTime object
val DateConverter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")

// Converter for dd-MM-yyyy format
val DateStandardConverter = DateTimeFormat.forPattern("dd-MM-yyyy")

/**
 * Replaces zone indication at the end of the date to 'Z'
 */
fun String.replaceZoneString(): String {
    return this.replace("+0000", "Z")
}

/**
 * Return string from string resource
 */
fun Int.asString(): String {
    return context.getString(this)
}