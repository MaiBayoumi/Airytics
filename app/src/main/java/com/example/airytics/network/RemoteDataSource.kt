package com.example.airytics.network

import com.example.airytics.model.Coordinate
import com.example.airytics.model.WeatherResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import retrofit2.Response

object RemoteDataSource: RemoteDataSourceInterface {
    override suspend fun getWeatherResponse(
        coordinate: Coordinate,
        language: String
    ): Flow<Response<WeatherResponse>> {
        val response = RetrofitHepler.apiService.getWeatherResponse(coordinate.latitude, coordinate.longitude, language)
        return  flowOf(response)

    }
}