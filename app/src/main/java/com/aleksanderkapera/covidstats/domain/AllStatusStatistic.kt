package com.aleksanderkapera.covidstats.domain

import com.aleksanderkapera.covidstats.room.AllStatusStatisticTable
import com.aleksanderkapera.covidstats.room.StatsDatabase
import com.aleksanderkapera.covidstats.room.asDomainModel
import com.aleksanderkapera.covidstats.util.DateStandardConverter
import org.joda.time.DateTime

data class AllStatusStatistic(
    val country: Country,
    val province: String,
    val city: String,
    val cityCode: String,
    val latitude: String,
    val longitude: String,
    var confirmed: Long,
    var deaths: Long,
    var recovered: Long,
    var active: Long,
    val date: DateTime
) {

    val formattedDate: String
        get() = DateStandardConverter.print(date)
}

/**
 * Convert [AllStatusStatisticTable] to [AllStatusStatistic]
 */
fun AllStatusStatistic.asDatabaseModel(): AllStatusStatisticTable {
    return AllStatusStatisticTable(
        countryName = country.countryName,
        countryCode = country.iso2,
        province = this.province,
        cityName = this.city,
        cityCode = this.cityCode,
        latitude = this.latitude,
        longitude = this.longitude,
        confirmed = this.confirmed,
        deaths = this.deaths,
        recovered = this.recovered,
        active = this.active,
        date = this.date.millis
    )
}