package com.example.covidstats.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.covidstats.domain.Status
import com.example.covidstats.domain.StatusEnum
import org.joda.time.DateTime

/**
 * Table representing statistics from one country, from one day and with specific type of status
 * (confirmed, deaths, recovered)
 */
@Entity
data class StatusTable(

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
 * Converts [StatusTable] to [Status]
 */
fun List<StatusTable>.asDomainModel(): List<Status> {
    return map {
        Status(
            countryName = it.countryName,
            countryCode = it.countryCode,
            latitude = it.latitude,
            longitude = it.longitude,
            cases = it.cases,
            status = StatusEnum.valueOf(it.status),
            date = DateTime(it.date)
        )
    }
}

