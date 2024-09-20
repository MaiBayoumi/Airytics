package com.example.airytics.network

import com.example.airytics.model.Coordinate
import com.example.airytics.model.WeatherResponse
import retrofit2.Response

object RemoteDataSource: RemoteDataSourceInterface {
    override suspend  fun getWeatherResponse(
        coordinate: Coordinate,
        language: String
    ): Response<WeatherResponse> {
        return RetrofitHepler.apiService.getWeatherResponse(coordinate.lat, coordinate.lon, language)

    }
}