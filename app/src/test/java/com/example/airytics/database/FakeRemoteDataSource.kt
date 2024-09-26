package com.example.airytics.database

import com.example.airytics.model.Coordinate
import com.example.airytics.model.WeatherForecastResponse
import com.example.airytics.model.WeatherResponse
import com.example.airytics.network.RemoteDataSourceInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import retrofit2.Response

class FakeRemoteDataSource(
    private var weatherResponse: WeatherResponse,
    private var weatherForecastResponse: WeatherForecastResponse
) : RemoteDataSourceInterface {

    override suspend fun getWeatherResponse(
        coordinate: Coordinate,
        language: String
    ): Response<WeatherResponse> {
        return Response.success(weatherResponse)
    }

    override suspend fun getWeatherForecast(
        coordinate: Coordinate,
        language: String
    ): Response<WeatherForecastResponse> {
        return Response.success(weatherForecastResponse)
    }
}
