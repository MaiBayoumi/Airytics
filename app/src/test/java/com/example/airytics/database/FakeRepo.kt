package com.example.airytics.model

import com.example.airytics.database.LocalDataSourceInterface
import com.example.airytics.location.LocationClientInterface
import com.example.airytics.network.ForecastState
import com.example.airytics.network.RemoteDataSourceInterface
import com.example.airytics.sharedpref.SettingSPInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

class FakeRepo(
    private val fakeRemoteSource: RemoteDataSourceInterface,
    private val fakeLocationClient: LocationClientInterface,
    private val fakeLocalSource: LocalDataSourceInterface,
    private val fakeSharedSetting: SettingSPInterface
) : RepoInterface {

    override fun getWeatherResponse(
        coordinate: Coordinate,
        language: String
    ): Flow<Response<WeatherResponse>> {
        return flow {
            try {
                // Use fake remote source to return mock weather response
                val response = fakeRemoteSource.getWeatherResponse(coordinate, language)
                emit(response)
            } catch (e: Exception) {
                // Log or handle the error in your fake repo
                emit(Response.error(500, null)) // Emit a fake error response for testing
            }
        }
    }

    override fun getWeatherForecast(
        coordinate: Coordinate,
        language: String
    ): Flow<ForecastState> {
        return flow {
            try {
                // Use fake remote source to return mock forecast response
                val forecastResponse = fakeRemoteSource.getWeatherForecast(coordinate, language)
                if (forecastResponse.isSuccessful && forecastResponse.body() != null) {
                    emit(ForecastState.Success(forecastResponse.body()!!))
                } else {
                    emit(ForecastState.Failure(forecastResponse.message()))
                }
            } catch (e: Exception) {
                emit(ForecastState.Failure(e.message.toString()))
            }
        }
    }

    override fun getCurrentLocation(): Flow<Coordinate> {
        // Use fake location client to return mock location
        return fakeLocationClient.getCurrentLocation()
    }

    override suspend fun insertPlaceToFav(place: Place) {
        fakeLocalSource.insertPlaceToFav(place) // Simulate insert to favorite places
    }

    override suspend fun deletePlaceFromFav(place: Place) {
        fakeLocalSource.deletePlaceFromFav(place) // Simulate deletion from favorite places
    }

    override fun getAllFavouritePlaces(): Flow<List<Place>> {
        return fakeLocalSource.getAllFavouritePlaces() // Simulate fetching all favorite places
    }

    override suspend fun insertCashedData(weatherResponse: WeatherResponse) {
        fakeLocalSource.insertCashedData(weatherResponse) // Simulate inserting cached data
    }

    override suspend fun insertCashedDataForecast(weatherForecastResponse: WeatherForecastResponse) {
        fakeLocalSource.insertCashedDataForcast(weatherForecastResponse) // Simulate inserting cached forecast data
    }

    override suspend fun deleteCashedData() {
        fakeLocalSource.deleteCashedData() // Simulate deletion of cached data
    }

    override fun getCashedData(): Flow<WeatherResponse>? {
        return fakeLocalSource.getCashedData() // Simulate fetching cached weather data
    }

    override fun getCashedDataForecast(): Flow<WeatherForecastResponse>? {
        return fakeLocalSource.getCashedDataForecast() // Simulate fetching cached forecast data
    }

    override suspend fun insertAlarm(alarmItem: AlarmItem) {
        fakeLocalSource.insertAlarm(alarmItem) // Simulate inserting an alarm
    }

    override suspend fun deleteAlarm(alarmItem: AlarmItem) {
        fakeLocalSource.deleteAlarm(alarmItem) // Simulate deleting an alarm
    }

    override fun getAllAlarms(): Flow<List<AlarmItem>> {
        return fakeLocalSource.getAllAlarms() // Simulate fetching all alarms
    }

    override fun writeStringToSettingSP(key: String, value: String) {
        fakeSharedSetting.writeStringToSettingSP(key, value) // Simulate writing to shared preferences
    }

    override fun readStringFromSettingSP(key: String): String {
        return fakeSharedSetting.readStringFromSettingSP(key) // Simulate reading from shared preferences
    }

    override fun writeFloatToSettingSP(key: String, value: Float) {
        fakeSharedSetting.writeFloatToSettingSP(key, value) // Simulate writing float to shared preferences
    }

    override fun readFloatFromSettingSP(key: String): Float {
        return fakeSharedSetting.readFloatFromSettingSP(key) // Simulate reading float from shared preferences
    }
}
