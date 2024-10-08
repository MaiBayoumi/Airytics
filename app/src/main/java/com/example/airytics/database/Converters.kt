package com.example.airytics.database

import androidx.room.TypeConverter
import com.example.airytics.model.*
import com.example.airytics.model.Current
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromCoord(coord: Coordinate?): String? {
        return gson.toJson(coord)
    }

    @TypeConverter
    fun toCoord(coordJson: String?): Coordinate? {
        return coordJson?.let {
            gson.fromJson(it, Coordinate::class.java)
        }
    }

    @TypeConverter
    fun fromClouds(clouds: Clouds?): String? {
        return gson.toJson(clouds)
    }

    @TypeConverter
    fun toClouds(cloudsJson: String?): Clouds? {
        return cloudsJson?.let {
            gson.fromJson(it, Clouds::class.java)
        }
    }

    @TypeConverter
    fun fromMain(main: Main?): String? {
        return gson.toJson(main)
    }

    @TypeConverter
    fun toMain(mainJson: String?): Main? {
        return mainJson?.let {
            gson.fromJson(it, Main::class.java)
        }
    }

    @TypeConverter
    fun fromSys(sys: Sys?): String? {
        return gson.toJson(sys)
    }

    @TypeConverter
    fun toSys(sysJason: String?): Sys? {
        return sysJason?.let {
            gson.fromJson(it, Sys::class.java)
        }
    }

    @TypeConverter
    fun fromWeatherList(weather: List<Weather>?): String? {
        return gson.toJson(weather)
    }

    @TypeConverter
    fun toWeatherList(weatherJson: String?): List<Weather>? {
        return weatherJson?.let {
            gson.fromJson(it, object : TypeToken<List<Weather>>() {}.type)
        }
    }

    @TypeConverter
    fun fromWind(wind: Wind?): String? {
        return wind?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toWind(windJson: String?): Wind? {
        return windJson?.let { gson.fromJson(it, Wind::class.java) }
    }

    @TypeConverter
    fun fromForecastItemList(value: List<ForecastItem>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toForecastItemList(value: String?): List<ForecastItem>? {
        val listType = object : TypeToken<List<ForecastItem>>() {}.type
        return Gson().fromJson(value, listType)
    }
    @TypeConverter
    fun fromForecastCity(value: City): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toForecastCity(value: String?): City{
        val listType = object : TypeToken<City>() {}.type
        return Gson().fromJson(value, listType)
    }


}
