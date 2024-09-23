package com.example.airytics.homefrag

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.airytics.R
import com.example.airytics.database.LocalDataSource
import com.example.airytics.databinding.FragmentHomeBinding
import com.example.airytics.hostedactivity.viewmodel.SharedViewModel
import com.example.airytics.hostedactivity.viewmodel.SharedViewModelFactory
import com.example.airytics.location.LocationClient
import com.example.airytics.model.Daily
import com.example.airytics.model.Repo
import com.example.airytics.model.WeatherForecastResponse
import com.example.airytics.model.WeatherResponse
import com.example.airytics.network.ApiState
import com.example.airytics.network.ForecastState
import com.example.airytics.network.RemoteDataSource
import com.example.airytics.sharedpref.SettingSharedPref
import com.example.airytics.utilities.Constants
import com.example.airytics.utilities.Functions
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var hourlyRecyclerAdapter: HourlyRecyclerAdapter
    private lateinit var dailyRecyclerAdapter: DailyRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iconAnimation()


        val factory = SharedViewModelFactory(
            Repo.getInstance(
                RemoteDataSource,
                LocationClient.getInstance(LocationServices.getFusedLocationProviderClient(view.context)),
                LocalDataSource.getInstance(requireContext()),
                SettingSharedPref.getInstance(requireContext())
            )
        )
        sharedViewModel = ViewModelProvider(requireActivity(), factory)[SharedViewModel::class.java]

        hourlyRecyclerAdapter = HourlyRecyclerAdapter()
        binding.rvHours.adapter = hourlyRecyclerAdapter
        dailyRecyclerAdapter = DailyRecyclerAdapter()
        binding.rvDays.adapter = dailyRecyclerAdapter


        lifecycleScope.launch(Dispatchers.IO) {
            launch(Dispatchers.IO) {
                sharedViewModel.weatherResponseForeStateFlow.collect { foreCastStateVar ->
                    when (foreCastStateVar) {
                        is ForecastState.Failure -> {
                            withContext(Dispatchers.Main) {  // Ensure Toast is shown on the main thread
                                Toast.makeText(
                                    requireContext(),
                                    foreCastStateVar.errorMessage,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        }

                        com.example.airytics.network.ForecastState.Loading -> {
                            withContext(Dispatchers.Main) {
                                binding.loadingLottie.visibility = View.VISIBLE
                            }
                        }

                        is ForecastState.Success -> {
                            Log.d("HASSAN","${foreCastStateVar.weatherForecast}")
                            val dailyList =
                                sharedViewModel.parseForecastResponse(foreCastStateVar.weatherForecast)
                            withContext(Dispatchers.Main) {
                                dailyRecyclerAdapter.submitList(dailyList)
                                binding.loadingLottie.visibility = View.GONE
                            }
                            insertCashedDataForecast(foreCastStateVar.weatherForecast)
                        }
                    }
                }
            }
            launch{
                sharedViewModel.weatherResponseStateFlow.collect { apiState ->
                    when (apiState) {
                        is ApiState.Success -> {
                            withContext(Dispatchers.Main) {
                                setDataToViews(apiState.weatherResponse)
                            }
                            insertCashedData(apiState.weatherResponse)
                        }
                        is ApiState.Loading -> {
                            withContext(Dispatchers.Main) {
                                binding.loadingLottie.visibility = View.VISIBLE
                            }
                        }
                        is ApiState.Failure -> {
                            withContext(Dispatchers.Main) {
                                binding.loadingLottie.visibility = View.GONE
                                Toast.makeText(
                                    requireContext(),
                                    apiState.errorMessage,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }

        }



        //for near me iconBtn
        if (sharedViewModel.readStringFromSettingSP(Constants.LOCATION) == Constants.MAP) {
            binding.ivNearMe.visibility = View.VISIBLE
        }
        binding.ivNearMe.setOnClickListener {
            if (sharedViewModel.checkConnection(requireContext())) {
                binding.prProgress.visibility = View.VISIBLE
                sharedViewModel.writeStringToSettingSP(
                    Constants.LOCATION,
                    Constants.GPS,
                )
                sharedViewModel.getLocation(view.context)
                binding.ivNearMe.visibility = View.GONE
            } else {
                Toast.makeText(
                    requireContext(),
                    "No Internet Connection",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun iconAnimation() {
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_icon)
        binding.ivWeather.startAnimation(animation)
    }


    @SuppressLint("SetTextI18n")
    private fun setDataToViews(weatherResponse: WeatherResponse) {
        makeViewsVisible()

        Functions.setIcon(weatherResponse.weather[0].icon, binding.ivWeather)

        binding.apply {
            tvLocationName.text = Functions.setLocationNameByGeoCoder(weatherResponse, requireContext())


            tvWeatherStatus.text = weatherResponse.weather[0].description.capitalize()

            tvDynamicPressure.text = String.format("%d %s", weatherResponse.main.pressure, getString(R.string.hpa))
            tvDynamicHumidity.text = String.format("%d %s", weatherResponse.main.humidity, getString(R.string.percentage))
            tvDynamicCloud.text = String.format("%d %s", weatherResponse.clouds.all, getString(R.string.percentage))

            val language = if (sharedViewModel.readStringFromSettingSP(Constants.LANGUAGE) == Constants.ARABIC) "ar" else "en"
            tvDate.text = Functions.fromUnixToString(weatherResponse.date, language)
            tvSunRise.text = Functions.fromUnixToStringTime(weatherResponse.sys.sunrise, language)
            tvSunSet.text = Functions.fromUnixToStringTime(weatherResponse.sys.sunset, language)

            val temperatureUnit = sharedViewModel.readStringFromSettingSP(Constants.TEMPERATURE)
            val temperature = when (temperatureUnit) {
                Constants.KELVIN -> String.format("%.1f°%s", weatherResponse.main.temp, getString(R.string.k))
                Constants.FAHRENHEIT -> String.format("%.1f°%s", (weatherResponse.main.temp - 273.15) * 9 / 5 + 32, getString(R.string.f))
                else -> String.format("%.1f°%s", weatherResponse.main.temp - 273.15, getString(R.string.c))
            }
            tvCurrentDegree.text = temperature



            val windSpeedUnit = sharedViewModel.readStringFromSettingSP(Constants.WIND_SPEED)
            val windSpeed = when (windSpeedUnit) {
                Constants.MILE_HOUR -> String.format("%.1f %s", weatherResponse.wind.speed * 2.237, getString(R.string.mile_hour))
                else -> String.format("%.1f %s", weatherResponse.wind.speed, getString(R.string.meter_sec))
            }
            tvDynamicWind.text = windSpeed

        }
        iconAnimation()

    }


    private fun makeViewsVisible() {
        binding.apply {
            binding.prProgress.visibility = View.GONE
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

    private fun insertCashedData(weatherResponse: WeatherResponse){
        if (sharedViewModel.checkConnection(requireContext())) {
            sharedViewModel.insertCachedData(weatherResponse)
        }
    }

    private fun insertCashedDataForecast(weatherForecastResponse: WeatherForecastResponse){
        if (sharedViewModel.checkConnection(requireContext())) {
            sharedViewModel.insertCachedDataForecast(weatherForecastResponse)
        }
    }
}