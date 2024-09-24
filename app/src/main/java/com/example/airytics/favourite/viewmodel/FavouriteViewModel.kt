package com.example.airytics.favourite.view

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.airytics.model.Place
import com.example.airytics.model.RepoInterface
import com.example.airytics.utilities.PermissionUtility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavouriteViewModel(private val repo: RepoInterface) : ViewModel() {

    private val _favouritePlacesMutableStateFlow: MutableStateFlow<List<Place>> =
        MutableStateFlow(emptyList())
    val favouritePlacesStateFlow: StateFlow<List<Place>> get() = _favouritePlacesMutableStateFlow

    fun checkConnection(context: Context): Boolean {
        return PermissionUtility.checkConnection(context)
    }

    fun deletePlaceFromFav(place: Place) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.deletePlaceFromFav(place)
        }
    }

    fun insertPlaceToFav(place: Place) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.insertPlaceToFav(place)
        }
    }

    fun getAllFavouritePlaces() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getAllFavouritePlaces().collectLatest {
                _favouritePlacesMutableStateFlow.value = it
            }
        }
    }
}