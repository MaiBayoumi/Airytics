import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.airytics.model.Place
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filteredLocations = MutableStateFlow<List<Place>>(emptyList())
    val filteredLocations: StateFlow<List<Place>> = _filteredLocations

    init {
        viewModelScope.launch {
            searchQuery
                .debounce(300) // Debounce to limit frequent updates
                .filter { it.isNotEmpty() } // Ignore empty searches
                .collect { query ->
                    //fetchLocations(query)
                }
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            _searchQuery.value = query
        }
    }

//    private suspend fun fetchLocations(query: String) {
//        val results = performLocationSearch(query) // Call your API
//        _filteredLocations.value = results
//    }

//    private suspend fun performLocationSearch(query: String): List<Place> {
//        return null
//    }
}
