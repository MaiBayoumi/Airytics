package com.example.airytics.network

import com.example.airytics.model.WeatherResponse
import com.example.airytics.utilities.Constants
import com.example.noaa.model.Weather
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("onecall?units=metric&appid=${Constants.WEATHER_API_KEY}")
    suspend fun getWeatherResponse(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("lang") language: String = "en"
    ): Response<WeatherResponse>
}