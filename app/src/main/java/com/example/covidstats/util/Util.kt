package com.example.covidstats.util

import org.joda.time.format.DateTimeFormat

// Converter used for converting API's date string to joda's DateTime object
val DateConverter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")
