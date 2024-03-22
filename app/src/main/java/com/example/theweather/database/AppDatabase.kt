package com.example.theweather.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WeatherCache::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
}
