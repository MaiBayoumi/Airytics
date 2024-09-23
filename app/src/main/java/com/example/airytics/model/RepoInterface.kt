package com.example.airytics.model

import com.example.airytics.network.ForecastState
import com.example.airytics.pojo.AlarmItem
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface RepoInterface {

     fun getWeatherResponse(
         coordinate: Coordinate,
         language: String
    ): Flow<Response<WeatherResponse>>

    fun getWeatherForecast(coordinate: Coordinate, language: String): Flow<ForecastState>

    fun getCurrentLocation():Flow<Coordinate>

    suspend fun insertPlaceToFav(place: Place)

    suspend fun deletePlaceFromFav(place: Place)

    fun getAllFavouritePlaces(): Flow<List<Place>>

    suspend fun insertCashedData(weatherResponse: WeatherResponse)

    suspend fun insertCashedDataForecast(weatherForecastResponse: WeatherForecastResponse)

    suspend fun deleteCashedData()

    fun getCashedData(): Flow<WeatherResponse>?

    //fun getCashedDataForecast(): Flow<WeatherForecastResponse>?

    fun writeStringToSettingSP(key: String, value: String)

    fun readStringFromSettingSP(key: String): String

    fun writeFloatToSettingSP(key: String, value: Float)

    fun readFloatFromSettingSP(key: String): Float

    suspend fun insertAlarm(alarmItem: AlarmItem)

    suspend fun deleteAlarm(alarmItem: AlarmItem)

    fun getAllAlarms(): Flow<List<AlarmItem>>


}