package com.example.airytics.model

import android.util.Log
import com.example.airytics.database.LocalDataSourceInterface
import com.example.airytics.location.LocationClientInterface
import com.example.airytics.network.ApiState
import com.example.airytics.network.ForecastState
import com.example.airytics.network.RemoteDataSourceInterface
import com.example.airytics.pojo.AlarmItem
import com.example.airytics.sharedpref.SettingSPInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

class Repo private constructor(
    private val remoteSource: RemoteDataSourceInterface,
    private val locationClient: LocationClientInterface,
    private val localSource: LocalDataSourceInterface,
    private val sharedSetting: SettingSPInterface

) : RepoInterface {

    companion object {
        private var instance: Repo? = null

        fun getInstance(
            remoteSource: RemoteDataSourceInterface,
            locationClient: LocationClientInterface,
            localSource: LocalDataSourceInterface,
            sharedSetting: SettingSPInterface

        ): Repo {
            return instance ?: synchronized(this) {
                instance ?: Repo(remoteSource, locationClient,localSource,sharedSetting).also { instance = it }
            }
        }

    }

    override  fun getWeatherResponse(
        coordinate: Coordinate,
        language: String
    ): Flow<Response<WeatherResponse>> {
        return flow {
            try {
                val response = remoteSource.getWeatherResponse(coordinate, language)
                emit(response)
            } catch (e: Exception) {
                Log.e("error", e.message.toString())
            }
        }
    }

//    @OptIn(FlowPreview::class)
//    override fun getWeatherByCountryName(
//        countryName: String,
//        language: String,
//        units: String
//    ): Flow<State<WeatherForLocation>> = flow {
//        emit(State.Loading)
//        try {
//            val response = remoteDataSource.getWeatherByCountryName(countryName,language,units)
//            if (response.isSuccessful && response.body() != null) {
//                emit(State.Success(response.body()!!))
//            } else {
//                emit(State.Error(response.message().orEmpty()))
//            }
//        } catch (e: Exception) {
//            emit(State.Error(e.message.orEmpty()))
//        }
//    }.timeout(10.seconds).catch {
//        emit(State.Error("Time out"))
//    }

    override fun getWeatherForecast(
        coordinate: Coordinate,
        language: String
    ): Flow<ForecastState> {
        return flow {
            try {
                val forecastResponse = remoteSource.getWeatherForecast(coordinate, language)
                if (forecastResponse.isSuccessful && forecastResponse.body() != null) {
                    emit(ForecastState.Success(forecastResponse.body()!!))
                }else{
                    emit (ForecastState.Failure(forecastResponse.message()))
                }
            } catch (e: Exception) {
                emit (ForecastState.Failure(e.message.toString()))
            }
        }
    }

    override fun getCurrentLocation(): Flow<Coordinate> {
        return locationClient.getCurrentLocation()
    }

    override suspend fun insertPlaceToFav(place: Place) {
        localSource.insertPlaceToFav(place)
    }

    override suspend fun deletePlaceFromFav(place: Place) {
        localSource.deletePlaceFromFav(place)
    }

    override fun getAllFavouritePlaces(): Flow<List<Place>> {
        return localSource.getAllFavouritePlaces()
    }

    override suspend fun insertCashedData(weatherResponse: WeatherResponse) {
        localSource.insertCashedData(weatherResponse)
    }

    override suspend fun deleteCashedData() {
        localSource.deleteCashedData()
    }

    override fun getCashedData(): Flow<WeatherResponse>? {
        return localSource.getCashedData()
    }

    override suspend fun insertAlarm(alarmItem: AlarmItem) {
        localSource.insertAlarm(alarmItem)
    }

    override suspend fun deleteAlarm(alarmItem: AlarmItem) {
        localSource.deleteAlarm(alarmItem)
    }

    override fun getAllAlarms(): Flow<List<AlarmItem>> {
        return localSource.getAllAlarms()
    }

    override fun writeStringToSettingSP(key: String, value: String) {
        sharedSetting.writeStringToSettingSP(key, value)
    }

    override fun readStringFromSettingSP(key: String): String {
        return sharedSetting.readStringFromSettingSP(key)
    }

    override fun writeFloatToSettingSP(key: String, value: Float) {
        sharedSetting.writeFloatToSettingSP(key, value)
    }

    override fun readFloatFromSettingSP(key: String): Float {
        return sharedSetting.readFloatFromSettingSP(key)
    }



}