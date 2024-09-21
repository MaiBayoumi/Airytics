package com.example.airytics.details.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.airytics.model.Coordinate
import com.example.airytics.model.RepoInterface
import com.example.airytics.network.ApiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val repo: RepoInterface
): ViewModel() {
    private val _weatherResponseMutableStateFlow: MutableStateFlow<ApiState> =
        MutableStateFlow(ApiState.Loading)
    val weatherResponseStateFlow: StateFlow<ApiState> get() = _weatherResponseMutableStateFlow

    fun getWeatherData(coordinate: Coordinate, language: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getWeatherResponse(coordinate, language)
                .catch { exception ->
                    // Handle the failure case
                    _weatherResponseMutableStateFlow.value = ApiState.Failure(exception.message ?: "Unknown error occurred")
                }
                .collectLatest { weatherResponse ->
                    if (weatherResponse.isSuccessful) {
                        // If the response is successful, update the state with the result
                        _weatherResponseMutableStateFlow.value = ApiState.Success(weatherResponse.body()!!)
                    } else {
                        // Handle API error response
                        _weatherResponseMutableStateFlow.value = ApiState.Failure(weatherResponse.message())
                    }
                }
        }
    }

    fun readStringFromSettingSP(key: String): String{
        return repo.readStringFromSettingSP(key)
    }
}