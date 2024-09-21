package com.example.airytics.map

import android.location.Geocoder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.airytics.R
import com.example.airytics.database.LocalDataSource
import com.example.airytics.databinding.FragmentMapBinding
import com.example.airytics.hostedactivity.view.HostedActivity
import com.example.airytics.hostedactivity.viewmodel.SharedViewModel
import com.example.airytics.hostedactivity.viewmodel.SharedViewModelFactory
import com.example.airytics.location.LocationClient
import com.example.airytics.model.Coordinate
import com.example.airytics.model.Place
import com.example.airytics.model.Repo
import com.example.airytics.network.RemoteDataSource
import com.example.airytics.sharedpref.SettingSharedPref
import com.example.airytics.utilities.Constants
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class mapFragment : Fragment() {
    private lateinit var binding: FragmentMapBinding
    private var marker: Marker? = null
    private var coordinate: Coordinate? = null
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var mapView: MapView

    override fun onStart() {
        super.onStart()
        super.onDestroy()
        val homeActivity = requireActivity() as HostedActivity
        homeActivity.binding.bottomNavigation.visibility = View.GONE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize OSM configuration
        Configuration.getInstance().load(context, requireContext().getSharedPreferences("osm_pref", 0))

        mapView = binding.map
        mapView.setMultiTouchControls(true)

        val mapController = mapView.controller
        mapController.setZoom(9.5)
        mapController.setCenter(GeoPoint(0.0, 0.0)) // Default starting location

        // Load bundle arguments
        val kind = arguments?.getString("kind")

        // Set up view model
        val factory = SharedViewModelFactory(
            Repo.getInstance(
                RemoteDataSource, LocationClient.getInstance(
                    LocationServices.getFusedLocationProviderClient(view.context),
                ),
                LocalDataSource.getInstance(requireContext()),
                SettingSharedPref.getInstance(requireContext())
            )
        )
        sharedViewModel = ViewModelProvider(requireActivity(), factory)[SharedViewModel::class.java]

        // Save location button handler
        binding.btnSaveLocation.setOnClickListener {
            if (sharedViewModel.checkConnection(requireContext())) {
                if (coordinate != null) {
                    showProgressBar()
                    handleLocationSave(kind)
                } else {
                    Toast.makeText(requireContext(), "please pick location", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(requireContext(), "No Internet Connection", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // OSM map handling
        mapViewHandler()
    }

    private fun mapViewHandler() {
        // Clear any existing overlays
        mapView.overlays.clear()

        // Add MapEventsOverlay to handle click events
        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let {
                    // Update the coordinate with the clicked point
                    coordinate = Coordinate(it.latitude, it.longitude)

                    // Remove previous marker if it exists
                    marker?.let { mapView.overlays.remove(it) }

                    // Create and place a new marker
                    marker = Marker(mapView).apply {
                        position = it
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    mapView.overlays.add(marker)
                    mapView.invalidate() // Refresh map
                }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                // Handle long press if needed
                return false
            }
        }

        // Add the event overlay to the map
        val mapEventsOverlay = org.osmdroid.views.overlay.MapEventsOverlay(mapEventsReceiver)
        mapView.overlays.add(mapEventsOverlay)
    }


    private fun handleLocationSave(kind: String?) {
        if (kind == Constants.REGULAR) {
            val language = sharedViewModel.readStringFromSettingSP(Constants.LANGUAGE)
            val langCode = if (language == Constants.ARABIC) "ar" else "en"
            sharedViewModel.getWeatherData(coordinate!!, langCode)
            sharedViewModel.writeFloatToSettingSP(Constants.LATITUDE, coordinate!!.lat.toFloat())
            sharedViewModel.writeFloatToSettingSP(Constants.LONGITUDE, coordinate!!.lon.toFloat())
            navigateBack()
        } else {
            var cityName = "Unknown Location"
            try {
                val addresses = Geocoder(requireContext()).getFromLocation(coordinate!!.lat, coordinate!!.lon, 5)
                cityName = addresses?.let {
                    if (it[0].locality != null) {
                        "${it[0].countryName} / ${it[0].locality}"
                    } else {
                        "${it[0].countryName} / ${it[0].adminArea}"
                    }
                } ?: "Unknown Location"
            } catch (_: Exception) { }

            sharedViewModel.insertPlaceToFav(
                Place(
                    cityName = cityName,
                    latitude = coordinate!!.lat,
                    longitude = coordinate!!.lon
                )
            )
            navigateBack()
        }
    }

    private fun showProgressBar() {
        binding.btnSaveLocation.visibility = View.GONE
        binding.prProgressMap.visibility = View.VISIBLE
    }

    private fun navigateBack() {
        val fragmentManager = parentFragmentManager
        fragmentManager.beginTransaction().commit()
        fragmentManager.popBackStack()
    }

    override fun onDestroy() {
        super.onDestroy()
        val homeActivity = requireActivity() as HostedActivity
        homeActivity.binding.bottomNavigation.visibility = View.VISIBLE
    }
}
