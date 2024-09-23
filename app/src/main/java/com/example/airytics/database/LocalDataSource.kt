package com.example.airytics.database

import android.content.Context
import com.example.airytics.model.WeatherResponse
import com.example.airytics.pojo.AlarmItem
import com.example.airytics.model.Place
import com.example.airytics.model.WeatherForecastResponse
import kotlinx.coroutines.flow.Flow

class LocalDataSource private constructor(context: Context): LocalDataSourceInterface {
    private val favouriteDao: FavouriteDao by lazy {
        FavouriteDatabase.getInstance(context).getDao()
    }

    companion object{
        private var instance: LocalDataSource? = null

        fun getInstance(context: Context): LocalDataSource{
            return instance?: LocalDataSource(context).also {
                instance = it
            }
        }
    }

    override suspend fun insertPlaceToFav(place: Place) {
        favouriteDao.insertPlaceToFav(place)
    }

    override suspend fun deletePlaceFromFav(place: Place) {
        favouriteDao.deletePlaceFromFav(place)
    }

    override fun getAllFavouritePlaces(): Flow<List<Place>> {
        return favouriteDao.getAllFavouritePlaces()
    }

    override suspend fun insertCashedData(weatherResponse: WeatherResponse) {
        favouriteDao.insertCashedData(weatherResponse)
    }

    override suspend fun insertCashedDataForcast(weatherForecastResponse: WeatherForecastResponse) {
        favouriteDao.insertCashedDataForcast(weatherForecastResponse)
    }

    override suspend fun deleteCashedData() {
        favouriteDao.deleteCashedData()
    }

    override fun getCashedData(): Flow<WeatherResponse> {
        return favouriteDao.getCashedData()
    }

    override fun getCashedDataForecast(): Flow<WeatherForecastResponse>? {
        return  favouriteDao.getCashedDataForecast()
    }

    override suspend fun insertAlarm(alarmItem: AlarmItem) {
        favouriteDao.insertAlarm(alarmItem)
    }

    override suspend fun deleteAlarm(alarmItem: AlarmItem) {
        favouriteDao.deleteAlarm(alarmItem)
    }

    override fun getAllAlarms(): Flow<List<AlarmItem>> {
        return favouriteDao.getAllAlarms()
    }
}
