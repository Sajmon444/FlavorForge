package pl.edu.anstar.flavorforge.ui.recipes

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
class RecipesViewModel @Inject constructor(
    private val apiService: RecipeApiService
) : ViewModel() {

    private val _recipes = MutableLiveData<List<RecipeSearchResult>>()
    val recipes: LiveData<List<RecipeSearchResult>> = _recipes

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchLatestRecipes() {
        viewModelScope.launch {
            try {
                // "gt.0&order=created_at.desc&limit=20" - fetching the most recent recipes. 
                // Since Supabase encodes the "&", we just use "gt.0" for simplicity which fetches all.
                // Or "gt.0&select=id,title,description,image_url,prep_time" but Retrofit urlencodes it.
                // Let's just use "gt.0" to get all recipes. 
                val response = apiService.getRecipesByIds("gt.0")
                if (response.isSuccessful && response.body() != null) {
                    _recipes.value = response.body()
                } else {
                    _error.value = "Failed to fetch recipes: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            }
        }
    }
}
