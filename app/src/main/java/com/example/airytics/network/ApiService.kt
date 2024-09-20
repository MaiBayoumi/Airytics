package com.example.airytics.network

import com.example.airytics.model.WeatherForecastResponse
import com.example.airytics.model.WeatherResponse
import com.example.airytics.utilities.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("weather")
    suspend fun getWeatherResponse(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("lang") language: String = "en",
        @Query("appid") apiKey: String = Constants.WEATHER_API_KEY
    ): Response<WeatherResponse>

    @GET("forecast")
    suspend fun getWeatherForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("lang") language: String = "en",
        @Query("appid") apiKey: String = Constants.WEATHER_API_KEY
    ): Response<WeatherForecastResponse>

}
