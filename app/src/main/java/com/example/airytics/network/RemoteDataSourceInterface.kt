package com.example.airytics.network

import com.example.airytics.model.Coordinate
import com.example.airytics.model.WeatherForecastResponse
import com.example.airytics.model.WeatherResponse
import retrofit2.Response

interface RemoteDataSourceInterface {
     suspend fun getWeatherResponse(coordinate: Coordinate, language: String): Response<WeatherResponse>
     suspend fun getWeatherForecast(coordinate: Coordinate, language: String): Response<WeatherForecastResponse>
}
