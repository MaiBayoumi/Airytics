package com.example.airytics.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.airytics.model.WeatherResponse
import com.example.airytics.model.AlarmItem
import com.example.airytics.model.Place
import com.example.airytics.model.WeatherForecastResponse


@Database(entities = [Place::class, WeatherResponse::class,WeatherForecastResponse::class, AlarmItem::class], version = 1)
@TypeConverters(Converters::class)
abstract class FavouriteDatabase: RoomDatabase() {

    abstract fun getDao(): FavouriteDao

    companion object {
        private var instance: FavouriteDatabase? = null

        fun getInstance(context: Context): FavouriteDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FavouriteDatabase::class.java,
                    "fav_db"
                ).build().also {
                    instance = it
                }
            }
        }
    }
}