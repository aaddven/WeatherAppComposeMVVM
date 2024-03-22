package com.example.theweather.di

import com.example.theweather.network.WeatherApi
import com.example.theweather.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideOpenWeatherApi(): WeatherApi{
        return Retrofit.Builder()
               .baseUrl(Constants.WEATHER_BASE_URL)
               .addConverterFactory(GsonConverterFactory.create())
               .build()
               .create(WeatherApi::class.java)
    }
}