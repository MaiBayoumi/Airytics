package com.example.airytics.network

import com.example.airytics.model.WeatherResponse

sealed class ApiState {
    class Success (val weatherResponse: WeatherResponse): ApiState()
    class Failure (val errorMessage : String): ApiState()
    object Loading: ApiState()
}