package com.example.airytics.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.airytics.model.WeatherResponse
import com.example.airytics.pojo.AlarmItem
import com.example.airytics.model.Place
import com.example.airytics.model.WeatherForecastResponse
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaceToFav(place: Place)

    @Delete
    suspend fun deletePlaceFromFav(place: Place)

    @Query("SELECT * FROM place")
    fun getAllFavouritePlaces(): Flow<List<Place>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashedData(weatherResponse: WeatherResponse)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashedDataForcast(weatherForecastResponse: WeatherForecastResponse)

    @Query("DELETE FROM WeatherResponse")
    suspend fun deleteCashedData()

    @Query("SELECT * FROM WeatherResponse")
    fun getCashedData(): Flow<WeatherResponse>


//    @Query("SELECT * FROM WeatherForecastResponse")
//    fun getCashedDataForecast(): Flow<WeatherForecastResponse>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarmItem: AlarmItem)

    @Delete
    suspend fun deleteAlarm(alarmItem: AlarmItem)

    @Query("SELECT * FROM alarm_item")
    fun getAllAlarms(): Flow<List<AlarmItem>>

}