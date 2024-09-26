package com.example.airytics.favourite.viewmodel

import app.cash.turbine.test
import com.example.airytics.favourite.view.FavouriteViewModel
import com.example.airytics.model.City
import com.example.airytics.model.Clouds
import com.example.airytics.model.Coordinate
import com.example.airytics.model.FakeRepo
import com.example.airytics.model.ForecastItem
import com.example.airytics.model.Main
import com.example.airytics.model.MainDispatcherRule
import com.example.airytics.model.Place
import com.example.airytics.model.RepoInterface
import com.example.airytics.model.Sys
import com.example.airytics.model.Weather
import com.example.airytics.model.WeatherForecastResponse
import com.example.airytics.model.WeatherResponse
import com.example.airytics.model.Wind
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class FavouriteViewModelTest{
    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val place1: Place = Place(0, "giza", 31.2125, 30.2121)
    private val place2: Place = Place(1, "cairo", 30.2125, 29.2121)
    private val place3: Place = Place(2, "giza", 32.2125, 28.2121)

    // Sample data for WeatherResponse
    private val weatherResponse: WeatherResponse = WeatherResponse(
        1, Clouds(1), 1, Coordinate(0.0, 1.0), 0,
        Main(0.0, 1, 1, 1, 1, 0.0, 1.0, 1.0),
        "", Sys(0, 0),
        1, 0,
        listOf(Weather("clear", "icon", 0, "Clear")),
        Wind(0, 0.0, 1.0)
    )
    private val weatherForecastResponse: WeatherForecastResponse = WeatherForecastResponse(
        id = 0,
        cod = "200", // Assuming a successful response code
        cnt = 1, // Number of forecast items
        list = listOf(
            ForecastItem(
                dt = 0,
                main = Main(
                    temp = 0.0,
                    pressure = 1,
                    humidity = 1,
                    temp_min = 1.0,
                    temp_max = 0.0,
                    feels_like = 1.0,
                    sea_level = 0,
                    grnd_level = 0
                ),
                clouds = Clouds(all = 1), // Assuming a simple clouds data
                wind = Wind(speed = 0.0, deg = 1, gust = 0.0),
                visibility = 1,
                pop = 0.1,
                sys = Sys(sunrise = 0, sunset = 0),
                weather = listOf(Weather(description = "", icon = "", id = 0, main = "")),
                dt_txt = ""
            ),
        ),
        city = City(
            id = 0,
            name = "TestCity", // Sample city name
            coord = Coordinate(lat = 0.0, lon = 1.0),
            country = "US", // Example country
            population = 0,
            timezone = 0,
            sunrise = 0,
            sunset = 0
        )
    )

    private lateinit var repo: RepoInterface
    private lateinit var favouriteViewModel: FavouriteViewModel


    @Before
    fun setUp() {

        repo = FakeRepo(weather = weatherResponse, stringReadValue = "", forecaste = weatherForecastResponse)
        favouriteViewModel = FavouriteViewModel(repo)
    }



    @Test
    fun insertPlaceToFavTest() = runBlocking{
        //when
        favouriteViewModel.insertPlaceToFav(place1)
        favouriteViewModel.getAllFavouritePlaces()
        var result: List<Place> = listOf()
        favouriteViewModel.favouritePlacesStateFlow.test {
            result = this.awaitItem()
        }

        //then
        assertTrue(result.contains(place1))
    }



    @Test
    fun deletePlaceFromFavTest() = runBlocking{
        //when
        favouriteViewModel.insertPlaceToFav(place1)
        favouriteViewModel.deletePlaceFromFav(place1)
        favouriteViewModel.getAllFavouritePlaces()
        var result : List<Place> = listOf()
        favouriteViewModel.favouritePlacesStateFlow.test {
            result = this.awaitItem()
        }

        //then
        assertFalse(result.contains(place1))
    }

    @Test
    fun getAllPlacesTest()= runBlocking{
        //when
        favouriteViewModel.insertPlaceToFav(place1)
        favouriteViewModel.insertPlaceToFav(place2)
        favouriteViewModel.insertPlaceToFav(place3)
        favouriteViewModel.getAllFavouritePlaces()

        var result: List<Place> = listOf()
        favouriteViewModel.favouritePlacesStateFlow.test {
            result = this.awaitItem()
        }

        //then
        assertEquals(listOf(place1, place2, place3), result)
    }


}

