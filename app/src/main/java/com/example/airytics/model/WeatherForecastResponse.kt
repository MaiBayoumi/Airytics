package com.example.airytics.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WeatherForecastResponse(
    @PrimaryKey val id: Int,
    val cod: String,
    val message: Int,
    val cnt: Int,
    val main:Main,
    val clouds: Clouds,
    val list: List<ForecastItem>,
    val city: City
)