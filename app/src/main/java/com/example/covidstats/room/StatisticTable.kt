package com.example.covidstats.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.covidstats.domain.Statistic
import com.example.covidstats.domain.StatusEnum
import org.joda.time.DateTime

/**
 * Table representing statistics from one country, from one day and with specific type of status
 * (confirmed, deaths, recovered)
 */
@Entity
data class StatisticTable(

    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val countryName: String,
    val countryCode: String,
    val latitude: String,
    val longitude: String,
    val cases: Long,
    val status: String,
    val date: Long
)

/**
 * Converts [StatisticTable] to [Statistic]
 */
fun List<StatisticTable>.asDomainModel(): List<Statistic> {
    return map {
        Statistic(
            countryName = it.countryName,
            countryCode = it.countryCode,
            latitude = it.latitude,
            longitude = it.longitude,
            cases = it.cases,
            status = StatusEnum.valueOf(it.status.toUpperCase()),
            date = DateTime(it.date)
        )
    }
}

