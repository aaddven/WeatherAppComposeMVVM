package com.example.theweather.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherCache(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cityName: String,
    val iconId: String,
    val temperature: Double,
    val mainDescription: String,
    val description: String,
    val pressure: Int,
    val humidity: Int,
    val windSpeed: Double,
    val dateTime: String,
    val sunrise: Long,
    val sunset: Long,
    val timestamp: Long
)