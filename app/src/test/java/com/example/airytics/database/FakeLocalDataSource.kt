package com.example.airytics.database

import com.example.airytics.model.AlarmItem
import com.example.airytics.model.Place
import com.example.airytics.model.WeatherForecastResponse
import com.example.airytics.model.WeatherResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeLocalDataSource ( private val places: MutableList<Place> = mutableListOf(),
                            private val alarms: MutableList<AlarmItem> = mutableListOf(),
                            private var weathers: MutableList<WeatherResponse> = mutableListOf(),
                            private var forecast: MutableList<WeatherForecastResponse> = mutableListOf())

    : LocalDataSourceInterface {


//    private val places: MutableList<Place> = mutableListOf()
//    private val alarms: MutableList<AlarmItem> = mutableListOf()
//    private var weathers: MutableList<WeatherResponse> = mutableListOf()

    override suspend fun insertPlaceToFav(place: Place) {
        places.add(place)
    }

    override suspend fun deletePlaceFromFav(place: Place) {
        places.remove(place)
    }

    override fun getAllFavouritePlaces(): Flow<List<Place>> {
        return flowOf(places)
    }

    override suspend fun insertCashedData(weatherResponse: WeatherResponse) {
        weathers.add(weatherResponse)
    }

    override suspend fun insertCashedDataForcast(weatherForecastResponse: WeatherForecastResponse) {
        forecast.add(weatherForecastResponse)
    }

    override suspend fun deleteCashedData() {
        weathers.clear()
    }

    override fun getCashedData(): Flow<WeatherResponse>? {
        return if (weathers.isNullOrEmpty()) {
            null
        } else {
            flowOf(weathers[0])
        }
    }

    override fun getCashedDataForecast(): Flow<WeatherForecastResponse>? {
        return if (forecast.isNullOrEmpty()) {
            null
        } else {
            flowOf(forecast[0])
        }
    }

    override suspend fun insertAlarm(alarmItem: AlarmItem) {
        alarms.add(alarmItem)
    }

    override suspend fun deleteAlarm(alarmItem: AlarmItem) {
        alarms.remove(alarmItem)
    }

    override fun getAllAlarms(): Flow<List<AlarmItem>> {
        return flowOf(alarms)
    }
}
