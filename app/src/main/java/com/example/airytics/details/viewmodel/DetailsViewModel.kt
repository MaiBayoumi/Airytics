package com.example.airytics.details.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.airytics.model.Coordinate
import com.example.airytics.model.Daily
import com.example.airytics.model.RepoInterface
import com.example.airytics.model.WeatherForecastResponse
import com.example.airytics.network.ApiState
import com.example.airytics.network.ForecastState
import com.example.airytics.model.Hourly
import com.example.airytics.utilities.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val repo: RepoInterface
): ViewModel() {

    init {
        getForecastData(Coordinate(0.0,0.0),"en")
    }

    private val _weatherResponseMutableStateFlow: MutableStateFlow<ApiState> =
        MutableStateFlow(ApiState.Loading)
    val weatherResponseStateFlow: StateFlow<ApiState> get() = _weatherResponseMutableStateFlow

    private val _weatherResponseMutableForecastStateFlow: MutableStateFlow<ForecastState> =
        MutableStateFlow(ForecastState.Loading)
    val weatherResponseForeStateFlow: StateFlow<ForecastState> get() = _weatherResponseMutableForecastStateFlow

    fun getWeatherData(coordinate: Coordinate, language: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getWeatherResponse(coordinate, language)
                .catch { exception ->
                    // Handle the failure case
                    _weatherResponseMutableStateFlow.value = ApiState.Failure(exception.message ?: "Unknown error occurred")
                }
                .collectLatest { weatherResponse ->
                    if (weatherResponse.isSuccessful) {
                        // If the response is successful, update the state with the result
                        _weatherResponseMutableStateFlow.value = ApiState.Success(weatherResponse.body()!!)
                    } else {
                        // Handle API error response
                        _weatherResponseMutableStateFlow.value = ApiState.Failure(weatherResponse.message())
                    }
                }
        }
    }

    fun getForecastData(coordinate: Coordinate,language: String){
        viewModelScope.launch (Dispatchers.IO ){
            repo.getWeatherForecast(coordinate,language)
                .collectLatest { weatherResponseForeList ->
                    _weatherResponseMutableForecastStateFlow.value= weatherResponseForeList
                }
        }
    }
     fun parseForecastResponse(response: WeatherForecastResponse): List<Daily> {
        val dailyList = mutableListOf<Daily>()
        val dayGroups = response.list.groupBy { forecast -> forecast.dt_txt.substring(0, 10) }.values.take(5)

        for (dayGroup in dayGroups) {
            val firstEntry = dayGroup.first()
            val day = firstEntry.dt_txt.substring(0, 10)
            val weatherDescription = firstEntry.weather[0].description.capitalize() // Should now be in the correct language
            val weatherIcon = firstEntry.weather[0].icon

            val temperatureUnit = readStringFromSettingSP(Constants.TEMPERATURE)

            val lowTemp = when (temperatureUnit) {
                Constants.KELVIN -> String.format("%.1f°K", dayGroup.minOf { it.main.temp_min })
                Constants.FAHRENHEIT -> String.format("%.1f°F", (dayGroup.minOf { it.main.temp_min } - 273.15) * 9 / 5 + 32)
                else -> String.format("%.1f°C", dayGroup.minOf { it.main.temp_min } - 273.15)
            }

            val highTemp = when (temperatureUnit) {
                Constants.KELVIN -> String.format("%.1f°K", dayGroup.maxOf { it.main.temp_max })
                Constants.FAHRENHEIT -> String.format("%.1f°F", (dayGroup.maxOf { it.main.temp_max } - 273.15) * 9 / 5 + 32)
                else -> String.format("%.1f°C", dayGroup.maxOf { it.main.temp_max } - 273.15)
            }

            dailyList.add(Daily(day, weatherDescription, lowTemp, highTemp, weatherIcon))
        }

        return dailyList
    }


    fun parseHourlyForecastResponse(response: WeatherForecastResponse): List<Hourly> {
        val hourlyList = mutableListOf<Hourly>()

        for (forecast in response.list) {
            val dateTime = forecast.dt // Assuming dt is Long
            //val weatherDescription = forecast.weather[0].description.capitalize()
            val weatherIcon = forecast.weather[0].icon

            // Get temperature unit preference
            val temperatureUnit = readStringFromSettingSP(Constants.TEMPERATURE)

            val temp = when (temperatureUnit) {
                Constants.KELVIN -> forecast.main.temp // Keep as is
                Constants.FAHRENHEIT -> (forecast.main.temp - 273.15) * 9 / 5 + 32 // Convert to Fahrenheit
                else -> forecast.main.temp - 273.15 // Convert to Celsius
            }


            // Assuming Hourly class requires pop, wind, and clouds
            hourlyList.add(Hourly(dateTime, temp, weatherIcon))
        }

        return hourlyList
    }


    fun readStringFromSettingSP(key: String): String{
        return repo.readStringFromSettingSP(key)
    }
}