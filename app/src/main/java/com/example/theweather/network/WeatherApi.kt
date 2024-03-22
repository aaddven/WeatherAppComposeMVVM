package com.example.theweather.network

import com.example.theweather.model.WeatherData.WeatherApiResponse
import com.example.theweather.utils.Constants
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Singleton

@Singleton
interface WeatherApi {
    @GET(value = "data/2.5/forecast")
    suspend fun getWeather(@Query("lat") query1: Double,
                           @Query("lon") query2: Double,
                           @Query("units") units: String = "metric",
                           @Query("appid") appid: String = Constants.WEATHER_API_KEY
                          ): WeatherApiResponse
}