package com.example.airytics.hostedactivity.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.airytics.model.Coordinate
import com.example.airytics.model.Daily
import com.example.airytics.model.Place
import com.example.airytics.model.RepoInterface
import com.example.airytics.model.WeatherForecastResponse
import com.example.airytics.model.WeatherResponse
import com.example.airytics.network.ApiState
import com.example.airytics.network.ForecastState
import com.example.airytics.utilities.Constants
import com.example.noaa.utilities.PermissionUtility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SharedViewModel(
    private val repo: RepoInterface
) : ViewModel() {
    init {
        getForecastData(Coordinate(0.0,0.0),"en")
    }

    private val _locationStatusMutableStateFlow: MutableStateFlow<String> = MutableStateFlow("")
    val locationStatusStateFlow: StateFlow<String> get() = _locationStatusMutableStateFlow

    private val _coordinateMutableStateFlow: MutableStateFlow<Coordinate> =
        MutableStateFlow(Coordinate(0.0, 0.0))
    val coordinateStateFlow: StateFlow<Coordinate> get() = _coordinateMutableStateFlow

    private val _weatherResponseMutableStateFlow: MutableStateFlow<ApiState> =
        MutableStateFlow(ApiState.Loading)
    val weatherResponseStateFlow: StateFlow<ApiState> get() = _weatherResponseMutableStateFlow

    private val _weatherResponseMutableForecastStateFlow: MutableStateFlow<ForecastState> =
        MutableStateFlow(ForecastState.Loading)
    val weatherResponseForeStateFlow: StateFlow<ForecastState> get() = _weatherResponseMutableForecastStateFlow

    fun getLocation(context: Context) {
        if (PermissionUtility.checkConnection(context)) {
            when (readStringFromSettingSP(Constants.LOCATION)) {
                Constants.GPS -> {
                    if (PermissionUtility.checkPermission(context)) {
                        if (PermissionUtility.isLocationIsEnabled(context)) {
                            viewModelScope.launch {
                                repo.getCurrentLocation().collectLatest {
                                    _coordinateMutableStateFlow.value = it
                                }
                            }
                        } else {
                            _locationStatusMutableStateFlow.value = Constants.SHOW_DIALOG
                        }
                    } else {
                        _locationStatusMutableStateFlow.value = Constants.REQUEST_PERMISSION
                    }
                }

                Constants.MAP -> {
                    _locationStatusMutableStateFlow.value = Constants.TRANSITION_TO_MAP
                }

                else -> {
                    _locationStatusMutableStateFlow.value = Constants.SHOW_INITIAL_DIALOG
                }
            }
        } else {
            getCachedData()
        }
    }

    fun getWeatherData(coordinate: Coordinate, language: String) {
        viewModelScope.launch(Dispatchers.IO) {
                repo.getWeatherResponse(coordinate, language)
                    .catch { exception ->
                        _weatherResponseMutableStateFlow.value = ApiState.Failure(exception.message ?: "Unknown error occurred")
                    }
                    .collectLatest { weatherResponse ->
                        if (weatherResponse.isSuccessful) {
                            _weatherResponseMutableStateFlow.value = ApiState.Success(weatherResponse.body()!!)
                        } else {
                            _weatherResponseMutableStateFlow.value = ApiState.Failure(weatherResponse.message())
                        }
                    }
        }
    }

    fun getForecastData(coordinate: Coordinate,language: String){
        viewModelScope.launch (Dispatchers.IO ){
            repo.getWeatherForecast(coordinate,language)
                .collectLatest { weatherResponseForeList ->
                    _weatherResponseMutableForecastStateFlow.value= weatherResponseForeList
                }
        }
    }


    fun insertPlaceToFav(place: Place) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.insertPlaceToFav(place)
        }
    }

    fun insertCachedData(weatherResponse: WeatherResponse) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.insertCashedData(weatherResponse)
        }
    }

    private fun getCachedData() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getCashedData()?.collect { cachedWeather ->
                try {
                    _weatherResponseMutableStateFlow.value = ApiState.Success(cachedWeather)
                } catch (exception: Exception) {
                    _weatherResponseMutableStateFlow.value = ApiState.Failure("Failed to load cached data")
                }
            }
        }
    }

    fun checkConnection(context: Context): Boolean {
        return PermissionUtility.checkConnection(context)
    }

    fun writeStringToSettingSP(key: String, value: String) {
        repo.writeStringToSettingSP(key, value)
    }

    fun readStringFromSettingSP(key: String): String {
        return repo.readStringFromSettingSP(key)
    }

    fun writeFloatToSettingSP(key: String, value: Float) {
        repo.writeFloatToSettingSP(key, value)
    }

    fun readFloatFromSettingSP(key: String): Float {
        return repo.readFloatFromSettingSP(key)
    }
}
