package com.example.airytics.model

data class WeatherForecastResponse(
    val cod: String,
    val message: Int,
    val cnt: Int,
    val main:Main,
    val clouds: Clouds,
    val list: List<ForecastItem>,
    val city: City
)