package com.example.airytics.setting

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.airytics.R
import com.example.airytics.database.LocalDataSource
import com.example.airytics.databinding.FragmentSettingBinding
import com.example.airytics.hostedactivity.viewmodel.SharedViewModel
import com.example.airytics.hostedactivity.viewmodel.SharedViewModelFactory
import com.example.airytics.location.LocationClient
import com.example.airytics.model.Repo
import com.example.airytics.network.RemoteDataSource
import com.example.airytics.sharedpref.SettingSharedPref
import com.example.airytics.utilities.Constants
import com.example.airytics.utilities.Functions
import com.google.android.gms.location.LocationServices


class settingFragment : Fragment() {

    private lateinit var binding: FragmentSettingBinding
    private lateinit var sharedViewModel: SharedViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSettingBinding.inflate(inflater, container, false)
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

        setDefaultRadioButtons()

        binding.radioGroupSettingLocation.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radio_setting_map) {
                sharedViewModel.writeStringToSettingSP(
                    Constants.LOCATION, Constants.MAP
                )
                val action = settingFragmentDirections.actionSettingFragmentToMapFragment()
                view.findNavController().navigate(action)
            } else {
                sharedViewModel.writeStringToSettingSP(Constants.LOCATION, Constants.GPS)
                sharedViewModel.getLocation(requireContext())
            }
        }

        binding.radioGroupSettingWind.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radio_setting_meter) {
                sharedViewModel.writeStringToSettingSP(
                    Constants.WIND_SPEED, Constants.METER_SEC
                )
            } else {
                sharedViewModel.writeStringToSettingSP(
                    Constants.WIND_SPEED, Constants.MILE_HOUR
                )
            }
        }


        binding.radioGroupSettingLanguage.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radio_setting_arabic) {
                sharedViewModel.writeStringToSettingSP(
                    Constants.LANGUAGE, Constants.ARABIC
                )
                Functions.changeLanguage(requireActivity(), "ar")
            } else {
                sharedViewModel.writeStringToSettingSP(
                    Constants.LANGUAGE, Constants.ENGLISH
                )
                Functions.changeLanguage(requireActivity(), "en")
            }
            Toast.makeText(requireContext(), "Restarting", Toast.LENGTH_LONG).show()
            restartApplication()
        }


        binding.radioGroupSettingNotification.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radio_setting_enable_notification) {
                sharedViewModel.writeStringToSettingSP(
                    Constants.NOTIFICATION, Constants.ENABLE
                )
            } else {
                sharedViewModel.writeStringToSettingSP(
                    Constants.NOTIFICATION, Constants.DISABLE
                )
            }
        }


        binding.radioGroupSettingTemp.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_setting_celsius -> {
                    sharedViewModel.writeStringToSettingSP(
                        Constants.TEMPERATURE, Constants.CELSIUS
                    )
                }
                R.id.radio_setting_kelvin -> {
                    sharedViewModel.writeStringToSettingSP(
                        Constants.TEMPERATURE, Constants.KELVIN
                    )
                }
                else -> {
                    sharedViewModel.writeStringToSettingSP(
                        Constants.TEMPERATURE, Constants.FAHRENHEIT
                    )
                }
            }
        }

    }


    private fun setDefaultRadioButtons() {
        if (sharedViewModel.readStringFromSettingSP(
                Constants.LOCATION
            ) == Constants.GPS
        ) {
            binding.radioGroupSettingLocation.check(R.id.radio_setting_gps)
        } else {
            binding.radioGroupSettingLocation.check(R.id.radio_setting_map)
        }

        if (sharedViewModel.readStringFromSettingSP(
                Constants.WIND_SPEED
            ) == Constants.MILE_HOUR
        ) {
            binding.radioGroupSettingWind.check(R.id.radio_setting_mile)
        } else {
            binding.radioGroupSettingWind.check(R.id.radio_setting_meter)
        }

        if (sharedViewModel.readStringFromSettingSP(
                Constants.LANGUAGE
            ) == Constants.ARABIC
        ) {
            binding.radioGroupSettingLanguage.check(R.id.radio_setting_arabic)
        } else {
            binding.radioGroupSettingLanguage.check(R.id.radio_setting_english)
        }

        if (sharedViewModel.readStringFromSettingSP(
                Constants.NOTIFICATION
            ) == Constants.DISABLE
        ) {
            binding.radioGroupSettingNotification.check(R.id.radio_setting_disable_notification)
        } else {
            binding.radioGroupSettingNotification.check(R.id.radio_setting_enable_notification)
        }

        if (sharedViewModel.readStringFromSettingSP(
                Constants.TEMPERATURE
            ) == Constants.KELVIN
        ) {
            binding.radioGroupSettingTemp.check(R.id.radio_setting_kelvin)
        } else if (sharedViewModel.readStringFromSettingSP(
                Constants.TEMPERATURE
            ) == Constants.FAHRENHEIT
        ) {
            binding.radioGroupSettingTemp.check(R.id.radio_fahrenheit)
        } else {
            binding.radioGroupSettingTemp.check(R.id.radio_setting_celsius)
        }
    }

    private fun restartApplication() {
        val intent = requireActivity().packageManager.getLaunchIntentForPackage(
            requireActivity().packageName
        )
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        requireActivity().finish()
        if (intent != null) {
            startActivity(intent)
        }
    }

}