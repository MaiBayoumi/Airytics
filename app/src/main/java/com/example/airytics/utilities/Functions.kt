package com.example.airytics.utilities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.location.Geocoder
import android.widget.ImageView
import com.example.airytics.R
import com.example.airytics.model.WeatherResponse
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Functions {

    fun setIcon(id: String, iv: ImageView){
        when (id) {
            "01d" -> iv.setImageResource(R.drawable.sun)
            "02d" -> iv.setImageResource(R.drawable._02d)
            "03d" -> iv.setImageResource(R.drawable._03d)
            "04d" -> iv.setImageResource(R.drawable._04n)
            "09d" -> iv.setImageResource(R.drawable._09n)
            "10d" -> iv.setImageResource(R.drawable._10d)
            "11d" -> iv.setImageResource(R.drawable._11d)
            "13d" -> iv.setImageResource(R.drawable._13d)
            "50d" -> iv.setImageResource(R.drawable._50d)
            "01n" -> iv.setImageResource(R.drawable._01n)
            "02n" -> iv.setImageResource(R.drawable._02n)
            "03n" -> iv.setImageResource(R.drawable._03d)
            "04n" -> iv.setImageResource(R.drawable._04n)
            "09n" -> iv.setImageResource(R.drawable._09n)
            "10n" -> iv.setImageResource(R.drawable._10n)
            "11n" -> iv.setImageResource(R.drawable._11d)
            "13n" -> iv.setImageResource(R.drawable._13d)
            "50n" -> iv.setImageResource(R.drawable._50d)
            else -> iv.setImageResource(R.drawable.cast)
        }
    }

    fun fromUnixToString(time: Int, lang: String): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale(lang))
        val date = Date(time * 1000L)
        return sdf.format(date).uppercase(Locale.ROOT)
    }

    fun fromUnixToStringTime(time: Int, lang: String): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale(lang))
        val date = Date(time * 1000L)
        return sdf.format(date).uppercase(Locale.ROOT)
    }

    fun formatDayOfWeek(timestamp: Long, context: Context, lang: String): String {
        val sdf = SimpleDateFormat("EEE", Locale(lang))
        val calendar: Calendar = Calendar.getInstance()

        val currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        calendar.timeInMillis = timestamp * 1000
        val targetDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

        val dayName = sdf.format(calendar.time).uppercase(Locale.ROOT)

        return when (targetDayOfYear) {
            currentDayOfYear -> dayName
            currentDayOfYear + 1 -> dayName
            else -> dayName
        }
    }

    fun formatTimestamp(timestamp: Long, lang: String): String {
        val sdf = SimpleDateFormat("h a", Locale(lang))
        val date = Date(timestamp * 1000)
        return sdf.format(date)
    }

    fun formatHourMinuteToString(hour: Int, minute: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    fun formatFromStringToLong(dateText: String, timeText: String): Long {
        val dateFormat = SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault())
        val dateAndTime = "$dateText $timeText}"
        val date = dateFormat.parse(dateAndTime)
        return date?.time ?: -1
    }

    fun formatLongToAnyString(dateTimeInMillis: Long, pattern: String): String {
        val resultFormat = SimpleDateFormat(pattern, Locale.getDefault())
        val date = Date(dateTimeInMillis)
        return resultFormat.format(date)
    }

    fun changeLanguage(activity: Activity, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val resources: Resources = activity.resources
        val config: Configuration = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    @SuppressLint("SetTextI18n")
    fun setLocationNameByGeoCoder(weatherResponse: WeatherResponse, context: Context): String {
        try {
            val x =
                Geocoder(context).getFromLocation(
                    weatherResponse.coord.lat,
                    weatherResponse.coord.lon,
                    5
                )

            return if (x != null && x[0].locality != null) {
                x[0].locality
            } else {
                weatherResponse.timezone.toString()
            }
        } catch (e: Exception) {
            return weatherResponse.timezone.toString()
        }
    }

}