package com.example.airytics.database

import com.example.airytics.model.WeatherResponse
import com.example.airytics.pojo.AlarmItem
import com.example.airytics.model.Place
import com.example.airytics.model.WeatherForecastResponse
import kotlinx.coroutines.flow.Flow

interface LocalDataSourceInterface {
    suspend fun insertPlaceToFav(place: Place)

    suspend fun deletePlaceFromFav(place: Place)

    fun getAllFavouritePlaces(): Flow<List<Place>>

    suspend fun insertCashedData(weatherResponse: WeatherResponse)

    suspend fun insertCashedDataForcast(weatherForecastResponse: WeatherForecastResponse)

    suspend fun deleteCashedData()

    fun getCashedData(): Flow<WeatherResponse>?

    fun getCashedDataForecast():Flow<WeatherForecastResponse>?

    suspend fun insertAlarm(alarmItem: AlarmItem)

    suspend fun deleteAlarm(alarmItem: AlarmItem)

    fun getAllAlarms(): Flow<List<AlarmItem>>
}