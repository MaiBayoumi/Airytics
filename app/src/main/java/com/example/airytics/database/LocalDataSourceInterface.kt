package com.example.airytics.database

import com.example.airytics.model.AlarmItem
import com.example.airytics.model.Place
import com.example.airytics.model.WeatherResponse
import kotlinx.coroutines.flow.Flow

interface LocalDataSourceInterface {
    suspend fun insertPlaceToFav(place: Place)

    suspend fun deletePlaceFromFav(place: Place)

    fun getAllFavouritePlaces(): Flow<List<Place>>

    suspend fun insertCashedData(weatherResponse: WeatherResponse)

    suspend fun deleteCashedData()

    fun getCashedData(): Flow<WeatherResponse>?

    suspend fun insertAlarm(alarmItem: AlarmItem)

    suspend fun deleteAlarm(alarmItem: AlarmItem)

    fun getAllAlarms(): Flow<List<AlarmItem>>
}