package com.example.airytics.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class WeatherResponse(
    @PrimaryKey val id: Int,  // Corrected primary key annotation
    @SerializedName("clouds") val clouds: Clouds,
    @SerializedName("cod") val cod: Int,
    @SerializedName("coord") val coord: Coordinate,
    @SerializedName("dt") val date: Int,
    @SerializedName("main") val main: Main,
    @SerializedName("name") val name: String,
    @SerializedName("sys") val sys: Sys,
    @SerializedName("timezone") val timezone: Int,
    @SerializedName("visibility") val visibility: Int,
    @SerializedName("weather") val weather: List<Weather>,
    @SerializedName("wind") val wind: Wind,

)
