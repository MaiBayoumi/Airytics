package com.example.airytics.network

import com.example.airytics.model.Coordinate
import com.example.airytics.model.WeatherResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface RemoteDataSourceInterface {
    suspend fun getWeatherResponse(coordinate: Coordinate, language: String): Flow<Response<WeatherResponse>>
}