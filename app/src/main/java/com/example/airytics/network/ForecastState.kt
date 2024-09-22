package com.example.airytics.network

import com.example.airytics.model.WeatherForecastResponse


 sealed class ForecastState {
    class Success (val weatherForecast: WeatherForecastResponse): ForecastState()
    class Failure (val errorMessage : String): ForecastState()
    object Loading: ForecastState()
}