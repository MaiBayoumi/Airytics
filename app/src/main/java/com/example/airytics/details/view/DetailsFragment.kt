package com.example.airytics.details.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.airytics.R
import com.example.airytics.database.LocalDataSource
import com.example.airytics.databinding.FragmentDetailsBinding
import com.example.airytics.details.viewmodel.DetailsViewModel
import com.example.airytics.details.viewmodel.DetailsViewModelFactory
import com.example.airytics.homefrag.DailyRecyclerAdapter
import com.example.airytics.homefrag.HourlyRecyclerAdapter
import com.example.airytics.hostedactivity.view.HostedActivity
import com.example.airytics.hostedactivity.view.TAG
import com.example.airytics.location.LocationClient
import com.example.airytics.model.Coordinate
import com.example.airytics.model.Repo
import com.example.airytics.model.WeatherResponse
import com.example.airytics.network.ApiState
import com.example.airytics.network.ForecastState
import com.example.airytics.network.RemoteDataSource
import com.example.airytics.sharedpref.SettingSharedPref
import com.example.airytics.utilities.Constants
import com.example.airytics.utilities.Functions
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailsFragment : Fragment() {
    private lateinit var binding: FragmentDetailsBinding
    private lateinit var detailViewModel: DetailsViewModel
    private lateinit var hourlyRecyclerAdapter: HourlyRecyclerAdapter
    private lateinit var dailyRecyclerAdapter: DailyRecyclerAdapter

    override fun onStart() {
        super.onStart()
        // Hide the bottom navigation when this fragment is visible
        val homeActivity = requireActivity() as HostedActivity
        homeActivity.binding.bottomNavigation.visibility = View.GONE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve the Place object from arguments
        val place = DetailsFragmentArgs.fromBundle(requireArguments()).place

        // ViewModel setup
        val factory = DetailsViewModelFactory(
            Repo.getInstance(
                RemoteDataSource, LocationClient.getInstance(
                    LocationServices.getFusedLocationProviderClient(view.context),
                ),
                LocalDataSource.getInstance(requireContext()),
                SettingSharedPref.getInstance(requireContext())
            )
        )
        detailViewModel = ViewModelProvider(this, factory)[DetailsViewModel::class.java]

        // Fetch weather data based on the user's language setting
        val language = if (detailViewModel.readStringFromSettingSP(Constants.LANGUAGE) == Constants.ARABIC) "ar" else "en"
        detailViewModel.getWeatherData(Coordinate(place.latitude, place.longitude), language)

        // RecyclerView adapters initialization
        hourlyRecyclerAdapter = HourlyRecyclerAdapter()
        binding.rvHours.adapter = hourlyRecyclerAdapter
        dailyRecyclerAdapter = DailyRecyclerAdapter()
        binding.rvDays.adapter = dailyRecyclerAdapter

        // Collecting weather forecast state
        lifecycleScope.launch(Dispatchers.IO) {
            launch {
                detailViewModel.weatherResponseForeStateFlow.collect { foreCastStateVar ->
                    when (foreCastStateVar) {
                        is ForecastState.Failure -> {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), foreCastStateVar.errorMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                        ForecastState.Loading -> {
                            withContext(Dispatchers.Main) {
                                binding.loadingLottie.visibility = View.VISIBLE
                            }
                        }
                        is ForecastState.Success -> {
                            val dailyList = detailViewModel.parseDailyForecastResponse(foreCastStateVar.weatherForecast)
                            val hourlyList = detailViewModel.parseHourlyForecastResponse(foreCastStateVar.weatherForecast)
                            withContext(Dispatchers.Main) {
                                dailyRecyclerAdapter.submitList(dailyList)
                                hourlyRecyclerAdapter.submitList(hourlyList)
                                binding.loadingLottie.visibility = View.GONE
                            }
                        }
                    }
                }
            }

            // Collecting weather data state
            launch {
                detailViewModel.weatherResponseStateFlow.collect { apiState ->
                    when (apiState) {
                        is ApiState.Success -> {
                            withContext(Dispatchers.Main) {
                                setDataToViews(apiState.weatherResponse)
                            }
                        }
                        is ApiState.Loading -> {
                            withContext(Dispatchers.Main) {
                                binding.loadingLottie.visibility = View.VISIBLE
                            }
                        }
                        is ApiState.Failure -> {
                            withContext(Dispatchers.Main) {
                                binding.loadingLottie.visibility = View.GONE
                                Toast.makeText(requireContext(), apiState.errorMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDataToViews(weatherResponse: WeatherResponse) {
        makeViewsVisible()

        // Setting weather icon
        Functions.setIcon(weatherResponse.weather[0].icon, binding.ivWeather)

        binding.apply {
            tvLocationName.text = Functions.setLocationNameByGeoCoder(weatherResponse, requireContext())
            tvWeatherStatus.text = weatherResponse.weather[0].description

            tvDynamicPressure.text = String.format("%d %s", weatherResponse.main.pressure, getString(R.string.hpa))
            tvDynamicHumidity.text = String.format("%d %s", weatherResponse.main.humidity, getString(R.string.percentage))
            tvDynamicCloud.text = String.format("%d %s", weatherResponse.clouds.all, getString(R.string.percentage))

            val language = if (detailViewModel.readStringFromSettingSP(Constants.LANGUAGE) == Constants.ARABIC) "ar" else "en"
            tvDate.text = Functions.fromUnixToString(weatherResponse.date, language)
            tvSunRise.text = Functions.fromUnixToStringTime(weatherResponse.sys.sunrise, language)
            tvSunSet.text = Functions.fromUnixToStringTime(weatherResponse.sys.sunset, language)

            // Setting temperature
            val temperatureUnit = detailViewModel.readStringFromSettingSP(Constants.TEMPERATURE)
            val temperature = when (temperatureUnit) {
                Constants.KELVIN -> String.format("%.1f°%s", weatherResponse.main.temp, getString(R.string.k))
                Constants.FAHRENHEIT -> String.format("%.1f°%s", (weatherResponse.main.temp - 273.15) * 9 / 5 + 32, getString(R.string.f))
                else -> String.format("%.1f°%s", weatherResponse.main.temp - 273.15, getString(R.string.c))
            }
            tvCurrentDegree.text = temperature

            // Setting wind speed
            val windSpeedUnit = detailViewModel.readStringFromSettingSP(Constants.WIND_SPEED)
            val windSpeed = when (windSpeedUnit) {
                Constants.MILE_HOUR -> String.format("%.1f %s", weatherResponse.wind.speed * 2.237, getString(R.string.mile_hour))
                else -> String.format("%.1f %s", weatherResponse.wind.speed, getString(R.string.meter_sec))
            }
            tvDynamicWind.text = windSpeed
        }
    }

    private fun makeViewsVisible() {
        binding.apply {
            loadingLottie.visibility = View.GONE
            tvLocationName.visibility = View.VISIBLE
            ivWeather.visibility = View.VISIBLE
            tvDate.visibility = View.VISIBLE
            tvCurrentDegree.visibility = View.VISIBLE
            tvWeatherStatus.visibility = View.VISIBLE
            cvDetails.visibility = View.VISIBLE
            rvDays.visibility = View.VISIBLE
            rvHours.visibility = View.VISIBLE
            sunRise.visibility = View.VISIBLE
            sunSet.visibility = View.VISIBLE
            tvSunRise.visibility = View.VISIBLE
            tvSunSet.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Show the bottom navigation when leaving this fragment
        val homeActivity = requireActivity() as HostedActivity
        homeActivity.binding.bottomNavigation.visibility = View.VISIBLE
    }
}
