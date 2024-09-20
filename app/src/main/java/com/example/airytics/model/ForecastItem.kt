package com.example.airytics.model

data class ForecastItem(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>,
    val clouds: Clouds,
    val wind: Wind,
    val visibility: Int,
    val pop: Double,  // Probability of precipitation
    val sys: Sys,
    val dt_txt: String
)