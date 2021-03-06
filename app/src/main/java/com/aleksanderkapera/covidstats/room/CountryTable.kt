package com.aleksanderkapera.covidstats.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aleksanderkapera.covidstats.domain.Country

/**
 * Database table representing country that can be fetched via api
 */
@Entity
data class CountryTable(

    @PrimaryKey
    val iso2: String,
    val slug: String,
    val countryName: String
)

/**
 * Converts [CountryTable] to domain data class [Country]
 */
fun List<CountryTable>.asDomainModel(): List<Country> {
    return map {
        Country(
            countryName = it.countryName,
            slug = it.slug,
            iso2 = it.iso2
        )
    }
}

fun CountryTable.asDomainModel(): Country {
    return Country(
        countryName = this.countryName,
        slug = this.slug,
        iso2 = this.iso2
    )
}