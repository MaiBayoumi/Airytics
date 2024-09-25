package com.example.airytics.favourite.view

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.airytics.R
import com.example.airytics.database.LocalDataSource
import com.example.airytics.databinding.FragmentFavouriteBinding
import com.example.airytics.location.LocationClient
import com.example.airytics.model.Place
import com.example.airytics.model.Repo
import com.example.airytics.network.RemoteDataSource
import com.example.airytics.sharedpref.SettingSharedPref
import com.example.airytics.utilities.Constants
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavouriteFragment : Fragment() {

    lateinit var binding: FragmentFavouriteBinding
    lateinit var favouriteRecyclerAdapter: FavouriteRecyclerAdapter
    lateinit var favouriteViewModel: FavouriteViewModel
    lateinit var place: Place

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavouriteBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.fabAddFav.setOnClickListener {
            val action =
                FavouriteFragmentDirections.actionFavouriteFragmentToMapFragment(Constants.FAVOURITE)
            view.findNavController().navigate(action)
        }

        val factory = FavouriteViewModelFactory(
            Repo.getInstance(
                RemoteDataSource,
                LocationClient.getInstance(LocationServices.getFusedLocationProviderClient(view.context)),
                LocalDataSource.getInstance(requireContext()),
                SettingSharedPref.getInstance(requireContext())
            )
        )
        favouriteViewModel = ViewModelProvider(this, factory)[FavouriteViewModel::class.java]
        favouriteViewModel.getAllFavouritePlaces()


        val itemTouchHelperCallBack = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                place = favouriteRecyclerAdapter.currentList[position]
                val mediaPlayer = MediaPlayer.create(context, R.raw.delete)
                mediaPlayer.start()

                mediaPlayer.setOnCompletionListener { mp -> mp.release() }

                favouriteViewModel.deletePlaceFromFav(place)

                val snackbar = Snackbar.make(binding.root, "Location deleted", Snackbar.LENGTH_LONG)
                snackbar.setAction("Undo") {
                    favouriteViewModel.insertPlaceToFav(place)
                    favouriteViewModel.getAllFavouritePlaces()
                }

                snackbar.show()
            }
        }

            val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallBack)
        itemTouchHelper.attachToRecyclerView(binding.rvFavourite)

        favouriteRecyclerAdapter = FavouriteRecyclerAdapter { selectedPlace ->
            if (favouriteViewModel.checkConnection(requireContext())) {
                val action = FavouriteFragmentDirections.actionFavouriteFragmentToDetailsFragment(selectedPlace)
                view.findNavController().navigate(action)
            } else {
                Toast.makeText(requireContext(), "No Internet Connection", Toast.LENGTH_SHORT).show()
            }
        }


        binding.rvFavourite.adapter = favouriteRecyclerAdapter

        lifecycleScope.launch {
            favouriteViewModel.favouritePlacesStateFlow.collectLatest {
                favouriteRecyclerAdapter.submitList(it)
            }
        }
    }


}