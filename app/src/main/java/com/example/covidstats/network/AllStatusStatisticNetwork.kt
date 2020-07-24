package com.example.covidstats.network

import com.example.covidstats.domain.AllStatusStatistic
import com.example.covidstats.room.AllStatusStatisticTable
import com.example.covidstats.room.StatsDatabase
import com.example.covidstats.room.asDomainModel
import com.example.covidstats.util.DateConverter
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
fun List<AllStatusStatisticNetwork>.asDomainModel(database: StatsDatabase): List<AllStatusStatistic> {
    return map {
        if (database.countriesDao.getCountryByIso(it.countryCode).value == null)
            throw Exception("No country in database")

        AllStatusStatistic(
            country = database.countriesDao.getCountryByIso(it.countryCode).value!!.asDomainModel(),
            province = it.province,
            city = it.cityName,
            cityCode = it.cityCode,
            latitude = it.latitude,
            longitude = it.longitude,
            confirmed = it.confirmed,
            deaths = it.deaths,
            recovered = it.recovered,
            active = it.active,
            date = DateTime.parse(it.date, DateConverter)
        )
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