package com.aleksanderkapera.covidstats.network

import com.aleksanderkapera.covidstats.domain.Country
import com.aleksanderkapera.covidstats.room.CountryTable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Used for communication with api. Holds information about a country.
 */
@JsonClass(generateAdapter = true)
data class CountryNetwork(
    @Json(name = "Country") val country: String,
    @Json(name = "Slug") val slug: String,
    @Json(name = "ISO2") val iso2: String
)

/**
 * Converts [CountryNetwork] to [Country]
 */
fun List<CountryNetwork>.asDomainModel(): List<Country> {
    return map {
        Country(
            countryName = it.country,
            slug = it.slug,
            iso2 = it.iso2
        )
    }
}

/**
 * Converts [CountryNetwork] to [CountryTable]
 */
fun List<CountryNetwork>.asDatabaseModel(): Array<CountryTable> {
    return map {
        CountryTable(
            countryName = it.country,
            slug = it.slug,
            iso2 = it.iso2
        )
    }.toTypedArray()
}