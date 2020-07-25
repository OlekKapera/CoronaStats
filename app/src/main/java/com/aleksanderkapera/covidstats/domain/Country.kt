package com.aleksanderkapera.covidstats.domain

/**
 * Data class representing available country to be fetched from server
 */
data class Country(
    val countryName: String,
    val slug: String,
    val iso2: String
)