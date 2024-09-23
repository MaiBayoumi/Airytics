package com.example.airytics.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class WeatherForecastResponse(
    @PrimaryKey val id: Int,
    @SerializedName("cod") val cod: String,
    //@SerializedName("message")val message: Int,
    @SerializedName("cnt")val cnt: Int,
    //@SerializedName("main")val main:Main,
    //@SerializedName("clouds")val clouds: Clouds,
    @SerializedName("list")val list: List<ForecastItem>,
    @SerializedName("city")val city: City
)