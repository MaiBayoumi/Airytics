package com.example.airytics.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class WeatherForecastResponse(
    @PrimaryKey val id: Int,
    @SerializedName("cod") val cod: String,
    @SerializedName("cnt")val cnt: Int,
    @SerializedName("list")val list: List<ForecastItem>,
    @SerializedName("city")val city: City
)