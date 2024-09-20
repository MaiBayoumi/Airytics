package com.example.airytics.hostedactivity.view

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.example.airytics.R
import com.example.airytics.database.LocalDataSource
import com.example.airytics.databinding.ActivityHostedBinding
import com.example.airytics.databinding.InitialDialogLayoutBinding
import com.example.airytics.hostedactivity.viewmodel.SharedViewModel
import com.example.airytics.hostedactivity.viewmodel.SharedViewModelFactory
import com.example.airytics.location.LocationClient
import com.example.airytics.model.Coordinate
import com.example.airytics.model.Repo
import com.example.airytics.network.RemoteDataSource
import com.example.airytics.sharedpref.SettingSharedPref
import com.example.airytics.utilities.Constants
import com.example.airytics.utilities.Functions
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val TAG = "location"
const val My_LOCATION_PERMISSION_ID = 5005

class HostedActivity : AppCompatActivity() {

    lateinit var binding: ActivityHostedBinding
    private lateinit var navController: NavController
    private lateinit var sharedViewModelFactory: SharedViewModelFactory
    private lateinit var sharedViewModel: SharedViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHostedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedViewModelFactory = SharedViewModelFactory(
            Repo.getInstance(
                RemoteDataSource,
                LocationClient.getInstance(LocationServices.getFusedLocationProviderClient(this)),
                LocalDataSource.getInstance(this),
                SettingSharedPref.getInstance(this)
            )
        )

        sharedViewModel =
            ViewModelProvider(this, sharedViewModelFactory)[SharedViewModel::class.java]

        setDefaultLanguage()

        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController)

        lifecycleScope.launch {
            sharedViewModel.locationStatusStateFlow.collect {
                withContext(Dispatchers.Main) {
                    when (it) {
                        Constants.SHOW_DIALOG -> showLocationDialog()
                        Constants.REQUEST_PERMISSION -> requestPermissions()
                        Constants.SHOW_INITIAL_DIALOG -> showInitialSettingDialog()
                        Constants.TRANSITION_TO_MAP -> transitionToMap()
                    }
                }
            }
        }

        lifecycleScope.launch {
            sharedViewModel.coordinateStateFlow.collect {
                Log.d("MAI", "onViewCreated: from fragment ${it.lat}")
                Log.d("MAI", "onViewCreated: from fragment ${it.lon}")
                if (it.lat != 0.0) {
                    if (sharedViewModel.readStringFromSettingSP(Constants.LANGUAGE) == Constants.ARABIC) {
                        sharedViewModel.getWeatherData(Coordinate(it.lat, it.lon), "ar")
                    } else {
                        sharedViewModel.getWeatherData(Coordinate(it.lat, it.lon), "en")
                    }
                }
            }
        }

    }


    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
        sharedViewModel.getLocation(this)
    }

    private fun transitionToMap() {
        val savedLatitude =
            sharedViewModel.readFloatFromSettingSP(Constants.LATITUDE).toDouble()
        val savedLongitude =
            sharedViewModel.readFloatFromSettingSP(Constants.LONGITUDE).toDouble()
        Log.d(TAG, "onResume: transition to map")
        if (savedLatitude == 0.0) {
            navController.navigate(R.id.mapFragment)
        } else {
            if (sharedViewModel.readStringFromSettingSP(Constants.LANGUAGE) == Constants.ARABIC
            ) {
                sharedViewModel.getWeatherData(Coordinate(savedLatitude, savedLongitude), "ar")
            } else {
                sharedViewModel.getWeatherData(Coordinate(savedLatitude, savedLongitude), "en")
            }
        }
    }


    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            My_LOCATION_PERMISSION_ID
        )
    }

    private fun showLocationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Location services are disabled. Do you want to enable them?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("No") { _, _ ->
                transitionToMap()
            }
        val dialog = builder.create()
        dialog.show()
    }

    private fun showInitialSettingDialog() {
        val bindingInitialLayoutDialog = InitialDialogLayoutBinding.inflate(layoutInflater)
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(bindingInitialLayoutDialog.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        bindingInitialLayoutDialog.btnOkInitial.setOnClickListener {
            val checkedBtn =
                bindingInitialLayoutDialog.radioGroupSettingLocationInitial.checkedRadioButtonId

            if (checkedBtn == bindingInitialLayoutDialog.radioSettingGpsInitial.id) {
                sharedViewModel.writeStringToSettingSP(Constants.LOCATION, Constants.GPS)
            } else {
                sharedViewModel.writeStringToSettingSP(Constants.LOCATION, Constants.MAP)
            }
            sharedViewModel.getLocation(this)

            dialog.dismiss()
        }
        dialog.show()
    }

    private fun setDefaultLanguage() {
        if (sharedViewModel.readStringFromSettingSP(Constants.LANGUAGE) == Constants.ARABIC) {
            Functions.changeLanguage(this, "ar")
        } else {
            Functions.changeLanguage(this, "en")
        }
    }
}
