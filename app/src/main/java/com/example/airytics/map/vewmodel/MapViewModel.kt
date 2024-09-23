import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.airytics.R
import com.example.airytics.model.Place
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MapViewModel(private val context: Context) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filteredLocations = MutableStateFlow<List<Place>>(emptyList())
    val filteredLocations: StateFlow<List<Place>> = _filteredLocations

    private val countries: List<String> by lazy {
        context.resources.getStringArray(R.array.countries).toList()
    }

    init {
        viewModelScope.launch {
            searchQuery
                .debounce(200) // Debounce to limit frequent updates
                .filter { it.isNotEmpty() }
                .collectLatest { query ->
                    fetchLocations(query)
                }
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            _searchQuery.value = query
        }
    }

    private  fun fetchLocations(query: String) {
        val results = performLocationSearch(query)
        _filteredLocations.value = results
    }

    private  fun performLocationSearch(query: String): List<Place> {
        val geocoder = Geocoder(context)
        return countries.filter { it.contains(query, ignoreCase = true) }
            .mapNotNull { countryName ->
                // Safely call getFromLocationName and handle null results
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
