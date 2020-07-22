package com.example.covidstats.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.covidstats.domain.Country

/**
 * Database table representing country that can be fetched via api
 */
@Entity
data class CountryTable(

    @PrimaryKey
    val slug: String,
    val countryName: String,
    val iso2: String
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