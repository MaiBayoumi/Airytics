package com.example.airytics.map.view

import MapViewModel
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MapFragment : Fragment() {
    private lateinit var binding: FragmentMapBinding
    private var marker: com.google.android.gms.maps.model.Marker? = null
    private var coordinate: Coordinate? = null
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var mapViewModel: MapViewModel
    private lateinit var googleMap: GoogleMap

    override fun onStart() {
        super.onStart()
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
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(requireContext())
        if (resultCode != ConnectionResult.SUCCESS) {
            googleApiAvailability.getErrorDialog(requireActivity(), resultCode, 0)?.show()
        } else {
            // Initialize your map or perform actions that require Google Play services
            googleMapHandler()
        }

        // Initialize ViewModel
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
        val mapFactory = MapViewModelFactory(requireContext())
        mapViewModel = ViewModelProvider(this, mapFactory)[MapViewModel::class.java]

        setupSearchView()

        // Save location button handler
        binding.btnSaveLocation.setOnClickListener {
            if (sharedViewModel.checkConnection(requireContext())) {
                if (coordinate != null) {
                    showProgressBar()
                    handleLocationSave(arguments?.getString("kind"))
                } else {
                    Toast.makeText(requireContext(), "Please pick a location", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "No Internet Connection", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun showProgressBar() {
        binding.btnSaveLocation.visibility = View.GONE
        binding.prProgressMap.visibility = View.VISIBLE
    }

    private fun googleMapHandler() {
        val supportMapFragment: SupportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        supportMapFragment.getMapAsync { map ->
            googleMap = map
            map.setOnMapClickListener { location ->
                marker?.remove()
                coordinate = Coordinate(location.latitude, location.longitude)
                marker = googleMap.addMarker(
                    MarkerOptions()
                        .position(location)
                )
            }
        }
    }


    private fun setupSearchView() {
        binding.etSearch.addTextChangedListener { text ->
            val query = text.toString()
            mapViewModel.search(query)

            lifecycleScope.launch {
                mapViewModel.filteredLocations.collect { places ->
                    if (places.isNotEmpty()) {
                        val firstPlace = places.first()
                        val latLng = LatLng(firstPlace.latitude, firstPlace.longitude)
                        placeMarkerOnMap(latLng) // Place the marker on the first result
                    }
                }
            }
        }
    }

    private fun placeMarkerOnMap(latLng: LatLng) {
        coordinate = Coordinate(latLng.latitude, latLng.longitude) // Set the coordinate
        marker?.remove() // Remove existing marker
        marker = googleMap.addMarker(
            MarkerOptions().position(latLng)
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f)) // Zoom to the location
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
