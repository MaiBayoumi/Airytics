package com.example.airytics.network

import com.example.airytics.model.Coordinate
import com.example.airytics.model.WeatherForecastResponse
import com.example.airytics.model.WeatherResponse
import retrofit2.Response

object RemoteDataSource: RemoteDataSourceInterface {

    override suspend fun getWeatherResponse(
        coordinate: Coordinate,
        language: String
    ): Response<WeatherResponse> {
        return RetrofitHepler.apiService.getWeatherResponse(
            coordinate.lat,
            coordinate.lon,
            language
        )

    }


    override suspend fun getWeatherForecast(
        coordinate: Coordinate,
        language: String
    ): Response<WeatherForecastResponse> {
        return RetrofitHepler.apiService.getWeatherForecast(
            coordinate.lat,
            coordinate.lon,
            language
        )
    }
}
