package com.aleksanderkapera.covidstats.network

import com.aleksanderkapera.covidstats.domain.AllStatusStatistic
import com.aleksanderkapera.covidstats.room.AllStatusStatisticTable
import com.aleksanderkapera.covidstats.room.StatsDatabase
import com.aleksanderkapera.covidstats.room.asDomainModel
import com.aleksanderkapera.covidstats.util.DateConverter
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.joda.time.DateTime

@JsonClass(generateAdapter = true)
data class AllStatusStatisticNetwork(
    @Json(name = "Country") val countryName: String,
    @Json(name = "CountryCode") val countryCode: String,
    @Json(name = "Province") val province: String,
    @Json(name = "City") val cityName: String,
    @Json(name = "CityCode") val cityCode: String,
    @Json(name = "Lat") val latitude: String,
    @Json(name = "Lon") val longitude: String,
    @Json(name = "Confirmed") val confirmed: Long,
    @Json(name = "Deaths") val deaths: Long,
    @Json(name = "Recovered") val recovered: Long,
    @Json(name = "Active") val active: Long,
    @Json(name = "Date") val date: String
)

/**
 * Convert [AllStatusStatisticNetwork] to [AllStatusStatistic]
 */
fun AllStatusStatisticNetwork.asDomainModel(database: StatsDatabase): AllStatusStatistic {
    val country = database.countriesDao().getCountryByIso(this.countryCode)
        ?: throw Exception("No country in database")

    return AllStatusStatistic(
        country = country.asDomainModel(),
        province = this.province,
        city = this.cityName,
        cityCode = this.cityCode,
        latitude = this.latitude,
        longitude = this.longitude,
        confirmed = this.confirmed,
        deaths = this.deaths,
        recovered = this.recovered,
        active = this.active,
        date = DateTime.parse(this.date, DateConverter)
    )
}

/**
 * Convert [AllStatusStatisticNetwork] to [AllStatusStatistic]
 */
fun List<AllStatusStatisticNetwork>.asDomainModel(database: StatsDatabase): List<AllStatusStatistic> {
    return map {
        it.asDomainModel(database)
    }
}

/**
 * Convert [AllStatusStatisticNetwork] to [AllStatusStatisticTable]
 */
fun List<AllStatusStatisticNetwork>.asDatabaseModel(): Array<AllStatusStatisticTable> {
    return map {
        AllStatusStatisticTable(
            date = DateTime.parse(it.date, DateConverter).millis,
            countryName = it.countryName,
            countryCode = it.countryCode,
            province = it.province,
            cityName = it.cityName,
            cityCode = it.cityCode,
            latitude = it.latitude,
            longitude = it.longitude,
            confirmed = it.confirmed,
            deaths = it.deaths,
            recovered = it.recovered,
            active = it.active
        )
    }.toTypedArray()
}