package com.example.airytics.map.vewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.airytics.model.Place
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filteredLocations = MutableStateFlow<List<Place>>(emptyList())
    val filteredLocations: StateFlow<List<Place>> = _filteredLocations

    init {
        viewModelScope.launch {
            searchQuery
                .debounce(300)
                .filterNotNull() // Ignore empty searches
                .collect { query ->
                    fetchLocations(query)
                }
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            _searchQuery.value = query
        }
    }

    private suspend fun fetchLocations(query: String) {

        val results = performLocationSearch(query) // Call your API
        _filteredLocations.value = results
    }

    private suspend fun performLocationSearch(query: String): List<Place> {

        return listOf(
            Place(1, "city1", 0.0, 0.0),
            Place(2, "city2", 1.0, 1.0)

        )
    }
}
