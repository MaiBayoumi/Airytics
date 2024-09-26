package com.example.airytics.model


import com.example.airytics.network.ForecastState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import retrofit2.Response

class FakeRepo(
    private val places: MutableList<Place> = mutableListOf(),
    private var weather: WeatherResponse,
    private var forecaste: WeatherForecastResponse,
    private var stringReadValue: String,
    private val alarms: MutableList<AlarmItem> = mutableListOf(), // For storing alarms
    private var cachedWeatherResponse: WeatherResponse? = null, // For cached weather
    private var cachedWeatherForecastResponse: WeatherForecastResponse? = null // For cached forecast
) : RepoInterface {

    override suspend fun deletePlaceFromFav(place: Place) {
        places.remove(place)
    }

    override suspend fun insertPlaceToFav(place: Place) {
        places.add(place)
    }

    override fun getAllFavouritePlaces(): Flow<List<Place>> {
        return flowOf(places)
    }

    override fun getWeatherResponse(
        coordinate: Coordinate,
        language: String
    ): Flow<Response<WeatherResponse>> {
        return flowOf(Response.success(weather))
    }

    override fun getWeatherForecast(
        coordinate: Coordinate,
        language: String
    ): Flow<ForecastState> {
        return flow { emit(ForecastState.Success(forecaste)) }
    }

    override fun readStringFromSettingSP(key: String): String {
        return stringReadValue
    }

    override fun writeStringToSettingSP(key: String, value: String) {
        stringReadValue = value
    }

    override fun getCurrentLocation(): Flow<Coordinate> {
        // Return a dummy location for testing
        return flow {
            emit(Coordinate(lat = 0.0, lon = 0.0))
        }
    }

    override suspend fun insertCashedData(weatherResponse: WeatherResponse) {
        cachedWeatherResponse = weatherResponse
    }

    override suspend fun insertCashedDataForecast(weatherForecastResponse: WeatherForecastResponse) {
        cachedWeatherForecastResponse = weatherForecastResponse
    }

    override suspend fun deleteCashedData() {
        cachedWeatherResponse = null
    }

    override fun getCashedData(): Flow<WeatherResponse> {
        return flow {
            emit(cachedWeatherResponse ?: throw Exception("No cached weather data available"))
        }
    }

    override fun getCashedDataForecast(): Flow<WeatherForecastResponse> {
        return flow {
            emit(cachedWeatherForecastResponse ?: throw Exception("No cached forecast data available"))
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

    override fun writeFloatToSettingSP(key: String, value: Float) {

    }

    override fun readFloatFromSettingSP(key: String): Float {

        return 0.0f // Placeholder
    }
}
