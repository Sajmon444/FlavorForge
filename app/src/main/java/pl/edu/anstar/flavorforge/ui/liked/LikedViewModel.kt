package pl.edu.anstar.flavorforge.ui.liked

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import pl.edu.anstar.flavorforge.data.model.RecipeSearchResult
import pl.edu.anstar.flavorforge.data.remote.RecipeApiService
import javax.inject.Inject

@HiltViewModel
class LikedViewModel @Inject constructor(
    private val apiService: RecipeApiService
) : ViewModel() {

    private val _recipes = MutableLiveData<List<RecipeSearchResult>>(emptyList())
    val recipes: LiveData<List<RecipeSearchResult>> = _recipes

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    fun loadFavoriteRecipes(favoriteIds: Set<String>) {
        if (favoriteIds.isEmpty()) {
            _recipes.value = emptyList()
            _error.value = "Brak ulubionych przepisów."
            return
        }

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val idsString = favoriteIds.joinToString(",")
                val response = apiService.getRecipesByIds("in.($idsString)")
                _isLoading.value = false

                if (response.isSuccessful && response.body() != null) {
                    _recipes.value = response.body()
                    if (response.body()!!.isEmpty()) {
                        _error.value = "Brak ulubionych przepisów."
                    }
                } else {
                    _error.value = "Nie udało się pobrać ulubionych przepisów: ${response.code()}"
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = "Błąd połączenia: ${e.message}"
            }
        }
    }
}
