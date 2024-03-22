package com.example.theweather.data

import com.example.theweather.model.WeatherData.WeatherApiResponse

data class WeatherDataWithTimestamp(
    val data: DataOrException<WeatherApiResponse, Boolean, Exception>,
    val timestamp: Long
)