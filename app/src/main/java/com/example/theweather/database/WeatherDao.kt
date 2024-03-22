package com.example.theweather.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeatherDao {

    // Inserting data
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weatherCache: WeatherCache)

    // Get Last Saved Snapshot
    @Query("SELECT * FROM weather_cache ORDER BY id DESC LIMIT 1")
    suspend fun getLastWeatherCache(): WeatherCache?

}