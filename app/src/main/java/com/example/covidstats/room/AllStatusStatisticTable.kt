package com.example.covidstats.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.covidstats.domain.AllStatusStatistic
import org.joda.time.DateTime

@Entity
data class AllStatusStatisticTable(

    @PrimaryKey
    val date: Long,
    val countryName: String,
    val countryCode: String,
    val province: String,
    val cityName: String,
    val cityCode: String,
    val latitude: String,
    val longitude: String,
    val confirmed: Long,
    val deaths: Long,
    val recovered: Long,
    val active: Long
)

/**
 * Convert [AllStatusStatisticTable] to [AllStatusStatistic]
 */
fun List<AllStatusStatisticTable>.asDomainModel(database: StatsDatabase): List<AllStatusStatistic> {
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
            date = DateTime(it.date)
        )
    }
}