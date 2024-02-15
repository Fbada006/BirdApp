import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.BirdImage

class BirdsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BirdsUiState())
    val uiState = _uiState.asStateFlow()

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    init {
        updateImages()
    }

    fun selectCategory(category: String) {
        _uiState.update {
            it.copy(selectedCategory = category)
        }
    }

    private fun updateImages() {
        viewModelScope.launch {
            val images = getImages()
            _uiState.update {
                it.copy(images = images)
            }
        }
    }

    private suspend fun getImages(): List<BirdImage> {
        return httpClient.get("https://sebi.io/demo-image-api/pictures.json")
            .body<List<BirdImage>>()
    }

    override fun onCleared() {
        super.onCleared()
        httpClient.close()
    }
}

data class BirdsUiState(
    val images: List<BirdImage> = emptyList(),
    val selectedCategory: String? = null
) {
    val selectedImages = if (selectedCategory == "All") images else images.filter { it.category == selectedCategory }

    fun getCategories(): Set<String> {
        val cats = images.map { it.category }.toMutableSet()
        cats.add("All")
        return cats.toSet()
    }
}