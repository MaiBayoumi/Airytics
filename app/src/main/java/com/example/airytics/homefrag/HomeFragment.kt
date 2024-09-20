package com.example.airytics.homefrag

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
import com.example.airytics.databinding.FragmentHomeBinding
import com.example.airytics.hostedactivity.viewmodel.SharedViewModel
import com.example.airytics.hostedactivity.viewmodel.SharedViewModelFactory
import com.example.airytics.location.LocationClient
import com.example.airytics.model.Repo
import com.example.airytics.model.WeatherResponse
import com.example.airytics.network.ApiState
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
            sharedViewModel.weatherResponseStateFlow.collect { apiState ->
                Log.d("HASSAN", "Received API state: $apiState") // Log the received state
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
                        Log.e("HASSAN", "Error: ${apiState.errorMessage}")
                        Log.d("HASSAN", "Using API Key: ${Constants.WEATHER_API_KEY}")
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



        //for near me iconBtn
        if (sharedViewModel.readStringFromSettingSP(Constants.LOCATION) == Constants.MAP) {
            binding.ivNearMe.visibility = View.VISIBLE
        }
        binding.ivNearMe.setOnClickListener {
            if (sharedViewModel.checkConnection(requireContext())) {
                binding.prProgress.visibility = View.VISIBLE
                sharedViewModel.writeStringToSettingSP(
                    Constants.LOCATION,
                    Constants.GPS
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



    @SuppressLint("SetTextI18n")
    private fun setDataToViews(weatherResponse: WeatherResponse) {
        makeViewsVisible()
        Log.d("HASSAN","current data ${weatherResponse}")
        Functions.setIcon(weatherResponse.weather[0].icon, binding.ivWeather)

        binding.apply {

            tvLocationName.text =
                Functions.setLocationNameByGeoCoder(weatherResponse, requireContext())
            tvWeatherStatus.text = weatherResponse.weather[0].description
            tvDynamicPressure.text =
                String.format("%d %s", weatherResponse.main.pressure, getString(R.string.hpa))
            tvDynamicHumidity.text = String.format(
                "%d %s",
                weatherResponse.main.humidity,
                getString(R.string.percentage)
            )
            tvDynamicCloud.text = String.format(
                "%d %s",
                weatherResponse.clouds,
                getString(R.string.percentage)
            )


            if (sharedViewModel.readStringFromSettingSP(Constants.LANGUAGE) == Constants.ARABIC) {
                tvDate.text = Functions.fromUnixToString(weatherResponse.date, "ar")
                tvSunRise.text = Functions.fromUnixToStringTime(weatherResponse.sys.sunrise, "ar")
                tvSunSet.text = Functions.fromUnixToStringTime(weatherResponse.sys.sunset, "ar")
            } else {
                tvDate.text = Functions.fromUnixToString(weatherResponse.date, "en")
                tvSunRise.text = Functions.fromUnixToStringTime(weatherResponse.sys.sunrise, "en")
                tvSunSet.text = Functions.fromUnixToStringTime(weatherResponse.sys.sunset, "en")
            }

            when (sharedViewModel.readStringFromSettingSP(Constants.TEMPERATURE)) {
                Constants.KELVIN -> tvCurrentDegree.text = String.format(
                    "%.1f°${getString(R.string.k)}",
                    weatherResponse.main.temp + 273.15
                )

                Constants.FAHRENHEIT -> tvCurrentDegree.text = String.format(
                    "%.1f°${getString(R.string.f)}",
                    weatherResponse.main.temp * 9 / 5 + 32
                )

                else -> tvCurrentDegree.text =
                    String.format("%.1f°${getString(R.string.c)}", weatherResponse.main.temp)
            }

            when (sharedViewModel.readStringFromSettingSP(Constants.WIND_SPEED)) {
                Constants.MILE_HOUR -> tvDynamicWind.text = String.format(
                    "%.1f ${getString(R.string.mile_hour)}",
                    weatherResponse.wind.speed * 2.237
                )

                else -> tvDynamicWind.text = String.format(
                    "%.1f ${getString(R.string.meter_sec)}",
                    weatherResponse.wind.speed
                )
            }

//            hourlyRecyclerAdapter.submitList(weatherResponse.hourly)
//            dailyRecyclerAdapter.submitList(weatherResponse.daily.filterIndexed { index, _ -> index != 0 }
//                .sortedWith(compareBy { it.dt }))
        }

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
            sharedViewModel.insertCashedData(weatherResponse)
        }
    }
}