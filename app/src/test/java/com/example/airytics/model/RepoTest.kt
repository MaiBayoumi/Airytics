import com.example.airytics.model.*
import com.example.airytics.database.FakeLocalDataSource
import com.example.airytics.database.FakeLocationClient
import com.example.airytics.database.FakeRemoteDataSource
import com.example.airytics.database.FakeSettingSharedPreferences
import com.example.airytics.network.ForecastState
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import retrofit2.Response


class RepoTest {

    private val place1: Place = Place(0, "giza", 31.2125, 30.2121)
    private val place2: Place = Place(1, "cairo", 30.2125, 29.2121)
    private val place3: Place = Place(2, "alexandria", 32.2125, 28.2121)

    private val alarm1: AlarmItem = AlarmItem(0, "Alarm1")
    private val alarm2: AlarmItem = AlarmItem(1, "Alarm2")
    private val alarm3: AlarmItem = AlarmItem(2, "Alarm3")

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

    private val coordinate: Coordinate = Coordinate(0.0, 0.0)

    private lateinit var fakeLocalSource: FakeLocalDataSource
    private lateinit var fakeRemoteSource: FakeRemoteDataSource
    private lateinit var fakeSettingSharedPref: FakeSettingSharedPreferences
    private lateinit var fakeLocationClient: FakeLocationClient
    private lateinit var repo: RepoInterface

    @Before
    fun setUp() {

        fakeLocalSource = FakeLocalDataSource(
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
        )
        fakeRemoteSource = FakeRemoteDataSource(weatherResponse, weatherForecastResponse)
        fakeSettingSharedPref = FakeSettingSharedPreferences("", 0.0f)
        fakeLocationClient = FakeLocationClient(coordinate)

        repo = Repo.getInstance(
            fakeRemoteSource,
            fakeLocationClient,
            fakeLocalSource,
            fakeSettingSharedPref
        )
    }


    //success weather response
    @Test
    fun `test getWeatherResponse returns valid response`() = runTest {
        //when
        val flow = repo.getWeatherResponse(coordinate, "en")

        //then
        flow.take(1).collect { response ->
            println("Response isSuccessful: ${response.isSuccessful}")
            println("Response body: ${response.body()}")

            assertTrue(response.isSuccessful)
            assertEquals(weatherResponse, response.body())
        }
    }


    @Test
    fun `test getWeatherForecast returns valid forecast state`() = runTest {

    }

    @Test
    fun `test getWeatherResponse handles error`() = runTest {

    }


    @Test
    fun `test getWeatherForecast handles error`() = runTest {
//        // Override the fakeRemoteSource to return a failed response
//        fakeRemoteSource = FakeRemoteDataSource(null, weatherForecastResponse) {
//            Response.error(404, null)
//        }
//        repo = Repo.getInstance(
//            fakeRemoteSource,
//            fakeLocationClient,
//            fakeLocalSource,
//            fakeSettingSharedPref
//        )
//
//        val flow = repo.getWeatherForecast(coordinate, "en")
//
//        flow.take(1).collect { state ->
//            assertTrue(state is ForecastState.Failure)
//            assertEquals("404 Not Found", state.message) // Adjust based on your error message handling
//        }
    }

    @Test
    fun getCurrentLocationTest() = runTest{
        //when
        val result = repo.getCurrentLocation()

        //then
        result.collectLatest {
            assertEquals(coordinate, it)
        }

    }


    @Test
    fun deletePlaceFromFavTest() = runTest{
        //when
        repo.deletePlaceFromFav(place1)

        var result : Boolean? = null
        repo.getAllFavouritePlaces().collectLatest {
            result = it.contains(place1)
        }

        //then
        Assert.assertEquals(false, result)

    }


    @Test
    fun insertPlaceToFavTest() = runTest{
        //when
        repo.insertPlaceToFav(place1)
        var result: Boolean? = null
        repo.getAllFavouritePlaces().collectLatest {
            result = it.contains(place1)
        }

        //then
        Assert.assertEquals(true, result)
        repo.deletePlaceFromFav(place1)

    }

    @Test
    fun writeStringToSPTest(){
        repo.writeStringToSettingSP("val1", "hassan")

        val result = repo.readStringFromSettingSP("val1")

        Assert.assertEquals("hassan", result)
    }

    @Test
    fun writeFloatToSPTest(){
        repo.writeFloatToSettingSP("val1", 2.2f)

        val result = repo.readFloatFromSettingSP("val2")

        Assert.assertEquals(2.2f, result)
    }

    @Test
    fun insertAlarmToDatabaseTest() = runTest{
        //when
        repo.insertAlarm(alarm1)
        var result: Boolean? = null
        repo.getAllAlarms().collectLatest {
            result = it.contains(alarm1)
        }

        //then
        Assert.assertEquals(true, result)
        repo.deleteAlarm(alarm1)
    }


    @Test
    fun deleteAlarmFromDatabaseTest() = runTest{
        //when
        repo.deleteAlarm(alarm1)

        var result : Boolean? = null
        repo.getAllAlarms().collectLatest {
            result = it.contains(alarm1)
        }

        //then
        Assert.assertEquals(false, result)

    }

    @Test
    fun insertCashedDataTest()= runTest{
        //when
        repo.insertCashedData(weatherResponse)
        val result = repo.getCashedData()

        //then
        result?.collectLatest {
            assertEquals(weatherResponse, it)
        }
        repo.deleteCashedData()
    }

    @Test
    fun insertCashedDataForecast()= runTest{
        //when
        repo.insertCashedDataForecast(weatherForecastResponse)
        val result = repo.getCashedDataForecast()

        //then
        result?.collectLatest {
            assertEquals(weatherForecastResponse, it)
        }
        repo.deleteCashedData()
    }

    @Test
    fun getCashedDataTest() = runTest {
        //when
        val result = repo.getCashedData()

        result?.collectLatest {
            assertEquals(weatherResponse, it)
        }
    }

    @Test
    fun getCashedDataForecast() = runTest {
        //when
        val result = repo.getCashedDataForecast()

        result?.collectLatest {
            assertEquals(weatherForecastResponse, it)
        }
    }
}


