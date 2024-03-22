package com.example.theweather.repository

import android.util.Log
import com.example.theweather.data.DataOrException
import com.example.theweather.database.WeatherCache
import com.example.theweather.database.WeatherDao
import com.example.theweather.model.WeatherData.City
import com.example.theweather.model.WeatherData.Clouds
import com.example.theweather.model.WeatherData.Coord
import com.example.theweather.model.WeatherData.Main
import com.example.theweather.model.WeatherData.Rain
import com.example.theweather.model.WeatherData.Sys
import com.example.theweather.model.WeatherData.WeatherApiResponse
import com.example.theweather.model.WeatherData.WeatherItem
import com.example.theweather.model.WeatherData.WeatherObject
import com.example.theweather.model.WeatherData.Wind
import com.example.theweather.network.WeatherApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


class WeatherRepository @Inject constructor(
    private val api: WeatherApi,
    private val weatherDao: WeatherDao
) {
    suspend fun getWeather(latQuery: Double, lonQuery: Double): DataOrException<WeatherApiResponse, Boolean, Exception> {
        // Show cached data first
        val lastCachedData = weatherDao.getLastWeatherCache()
        lastCachedData?.let {
            // Convert cached data to API response and return it
            Log.d("WeatherRepo", "Using cached weather data for ${it.cityName}")
            return DataOrException(data = convertCacheToApiResponse(it))
        }

        // If no cached data is available, fetch from network
        return fetchAndCacheWeather(latQuery, lonQuery)

    }

    private suspend fun fetchAndCacheWeather(latQuery: Double, lonQuery: Double): DataOrException<WeatherApiResponse, Boolean, Exception> {
        return withContext(Dispatchers.IO) {
            try {

                Log.d("WeatherRepo", "Fetching and Caching Data")
                val response = api.getWeather(latQuery, lonQuery)
                cacheWeatherData(response)
                DataOrException(data = response)

            } catch (e: Exception) {
                Log.d("WeatherRepo", "Network fetch failed: $e")
                DataOrException(e = e)
            }
        }
    }

    private suspend fun cacheWeatherData(apiResponse: WeatherApiResponse) {
        val firstWeatherItem = apiResponse.list.firstOrNull()
        val weatherCache = WeatherCache(
            cityName = apiResponse.city.name,
            iconId = firstWeatherItem?.weather?.firstOrNull()?.icon ?: "",
            temperature = firstWeatherItem?.main?.temp ?: 0.0,
            mainDescription = firstWeatherItem?.weather?.firstOrNull()?.main ?: "",
            description = firstWeatherItem?.weather?.firstOrNull()?.description ?: "",
            pressure = firstWeatherItem?.main?.pressure ?: 0,
            humidity = firstWeatherItem?.main?.humidity ?: 0,
            windSpeed = firstWeatherItem?.wind?.speed ?: 0.0,
            dateTime = firstWeatherItem?.dt_txt ?: "",
            sunrise = apiResponse.city.sunrise.toLong(),
            sunset = apiResponse.city.sunset.toLong(),
            timestamp = System.currentTimeMillis()
        )
        weatherDao.insert(weatherCache)
        Log.d("WeatherRepo", "Caching weather data for ${apiResponse.city.name}")

    }

    private fun convertCacheToApiResponse(cache: WeatherCache): WeatherApiResponse {
        val weatherItem = WeatherItem(
            clouds = Clouds(0),
            dt = 0,
            dt_txt = cache.dateTime,
            main = Main(
                temp = cache.temperature,
                pressure = cache.pressure,
                humidity = cache.humidity,
                temp_min = cache.temperature,
                temp_max = cache.temperature,
                sea_level = 0,
                grnd_level = 0,
                temp_kf = 0.0,
                feels_like = cache.temperature
            ),
            pop = 0.0,
            rain = Rain(0.0),
            sys = Sys(""),
            visibility = 0,
            weather = listOf(
                WeatherObject(
                    description = cache.description,
                    icon = cache.iconId,
                    id = 0,
                    main = cache.mainDescription
                )
            ),
            wind = Wind(
                speed = cache.windSpeed,
                deg = 0,
                gust = 0.0
            )
        )

        return WeatherApiResponse(
            city = City(
                coord = Coord(0.0, 0.0),
                country = "",
                id = 0,
                name = cache.cityName,
                population = 0,
                sunrise = cache.sunrise.toInt(),
                sunset = cache.sunset.toInt(),
                timezone = 0
            ),
            cnt = 1,
            cod = "",
            list = listOf(weatherItem),
            message = 0

        )

        Log.d("WeatherRepo", "Converted Cache to API Response")
    }

    suspend fun isCachedDataAvailable(): Boolean {
        return weatherDao.getLastWeatherCache() != null
    }


    suspend fun getCachedWeatherData(): WeatherApiResponse? {
        val cachedData = weatherDao.getLastWeatherCache()
        return cachedData?.let { convertCacheToApiResponse(it) }
    }

}

