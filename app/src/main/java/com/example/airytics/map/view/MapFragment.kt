package com.example.airytics.map.view

import MapViewModel
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.airytics.R
import com.example.airytics.database.LocalDataSource
import com.example.airytics.databinding.FragmentMapBinding
import com.example.airytics.hostedactivity.view.HostedActivity
import com.example.airytics.hostedactivity.viewmodel.SharedViewModel
import com.example.airytics.hostedactivity.viewmodel.SharedViewModelFactory
import com.example.airytics.location.LocationClient
import com.example.airytics.map.vewmodel.MapViewModelFactory
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            googleMapHandler()
        }

        val factory = SharedViewModelFactory(
            Repo.getInstance(
                RemoteDataSource, LocationClient.getInstance(
                    LocationServices.getFusedLocationProviderClient(requireContext()),
                ),
                LocalDataSource.getInstance(requireContext()),
                SettingSharedPref.getInstance(requireContext())
            )
        )

        sharedViewModel = ViewModelProvider(requireActivity(), factory)[SharedViewModel::class.java]

        val mapFactory = MapViewModelFactory(requireActivity().application)
        mapViewModel = ViewModelProvider(this, mapFactory)[MapViewModel::class.java]

        setupSearchView()

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

    private fun setupSearchView() {
        val debounceTime = 300L
        var lastQuery: String? = null
        val handler = Handler(Looper.getMainLooper())

        binding.etSearch.addTextChangedListener { text ->
            val query = text.toString()
            lastQuery = query

            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({
                if (query == lastQuery) {
                    mapViewModel.search(query)
                }
            }, debounceTime)
        }

        lifecycleScope.launch {
            mapViewModel.filteredLocations.collect { places ->
                if (places.isNotEmpty()) {
                    val firstPlace = places.first()
                    val latLng = LatLng(firstPlace.latitude, firstPlace.longitude)
                    placeMarkerOnMap(latLng)
                }
            }
        }
    }

    private fun placeMarkerOnMap(latLng: LatLng) {
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                coordinate = Coordinate(latLng.latitude, latLng.longitude)
                marker?.remove()
                marker = googleMap.addMarker(
                    MarkerOptions().position(latLng)
                )
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            }
        }
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
                val addresses = Geocoder(requireActivity().application).getFromLocation(coordinate!!.lat, coordinate!!.lon, 5)
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
