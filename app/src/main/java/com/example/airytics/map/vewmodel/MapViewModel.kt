import android.app.Application
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.airytics.R
import com.example.airytics.model.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(FlowPreview::class)
class MapViewModel(application: Application) : ViewModel() {

   // private val context: Context = application.applicationContext

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filteredLocations = MutableStateFlow<List<Place>>(emptyList())
    val filteredLocations: StateFlow<List<Place>> = _filteredLocations

    private val countries: List<String> by lazy {
        application.resources.getStringArray(R.array.countries).toList()
    }

    private val geocoder: Geocoder by lazy {
        Geocoder(application)
    }

    init {
        viewModelScope.launch {
            searchQuery
                .debounce(300) // Limit frequent updates
                .filter { it.isNotEmpty() } // Ignore empty queries
                .distinctUntilChanged() // Process only if the query changes
                .collectLatest { query ->
                    fetchLocations(query) // Perform search on each new query
                }
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            _searchQuery.value = query
        }
    }

    private fun fetchLocations(query: String) {
        // Offload the location search to a background thread
        viewModelScope.launch(Dispatchers.IO) {
            val results = performLocationSearch(query)
            withContext(Dispatchers.Main) {
                _filteredLocations.value = results
            }
        }
    }

    private fun performLocationSearch(query: String): List<Place> {
        return countries.filter { it.contains(query, ignoreCase = true) }
            .mapNotNull { countryName ->
                val addresses = geocoder.getFromLocationName(countryName, 1) ?: return@mapNotNull null
                addresses.firstOrNull()?.let {
                    Place(
                        cityName = it.locality ?: it.adminArea ?: countryName,
                        latitude = it.latitude,
                        longitude = it.longitude
                    )
                }
            }
    }
}
