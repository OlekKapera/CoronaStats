package com.aleksanderkapera.covidstats.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.aleksanderkapera.covidstats.domain.AllStatusStatistic
import org.joda.time.DateTime

@Entity(indices = [Index(value = ["date", "countryCode"], unique = true)])
data class AllStatusStatisticTable(

    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
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
        it.asDomainModel(database)
    }
}

/**
 * Convert [AllStatusStatisticTable] to [AllStatusStatistic]
 */
fun AllStatusStatisticTable.asDomainModel(database: StatsDatabase): AllStatusStatistic {
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
        date = DateTime(this.date)
    )
}