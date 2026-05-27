package pl.edu.anstar.flavorforge.ui.recipes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import pl.edu.anstar.flavorforge.data.local.SessionManager
import pl.edu.anstar.flavorforge.data.model.RecipeSearchResult
import pl.edu.anstar.flavorforge.data.remote.RecipeApiService
import javax.inject.Inject

@HiltViewModel
class RecipesViewModel @Inject constructor(
    private val apiService: RecipeApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _recipes = MutableLiveData<List<RecipeSearchResult>>()
    val recipes: LiveData<List<RecipeSearchResult>> = _recipes

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private var allRecipes: List<RecipeSearchResult> = emptyList()

    fun fetchLatestRecipes() {
        viewModelScope.launch {
            try {
                // "gt.0" fetches all recipes.
                val response = apiService.getRecipesByIds("gt.0")
                if (response.isSuccessful && response.body() != null) {
                    allRecipes = response.body()!!
                    // Initially apply settings filtering
                    clearFilters()
                } else {
                    _error.value = "Failed to fetch recipes: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            }
        }
    }

    fun filterRecipes(
        easy: Boolean,
        medium: Boolean,
        hard: Boolean,
        maxTime: Int?,
        maxCalories: Int?,
        vege: Boolean,
        vegan: Boolean,
        glutenFree: Boolean
    ) {
        val sp = sessionManager.getSettingsPrefs()
        val settingsPrepProgress = sp.getInt("settings_prep_time", 12)
        val settingsMaxTime = settingsPrepProgress * 15 // min

        val settingsCaloriesProgress = sp.getInt("settings_calories", 54)
        val settingsMaxCalories = 300 + settingsCaloriesProgress * 50 // kcal

        // Overrides: custom drawer constraints take precedence over global settings
        val activeMaxTime = maxTime ?: settingsMaxTime
        val activeMaxCalories = maxCalories ?: settingsMaxCalories

        val filtered = allRecipes.filter { recipe ->
            // --- Global settings / Drawer overrides constraints ---
            val prepTimeLimitMatches = recipe.prepTime?.let { it <= activeMaxTime } ?: true
            val caloriesLimitMatches = recipe.caloriesTotal?.let { it <= activeMaxCalories } ?: true

            if (!prepTimeLimitMatches || !caloriesLimitMatches) {
                return@filter false
            }

            // --- Drawer specific filters ---
            // 1. Difficulty Filter
            val diffMatches = if (!easy && !medium && !hard) {
                true
            } else {
                val diff = recipe.difficulty?.lowercase() ?: ""
                val isEasy = diff.contains("easy") || diff.contains("łatw") || diff == "1"
                val isMedium = diff.contains("medium") || diff.contains("średn") || diff == "2"
                val isHard = diff.contains("hard") || diff.contains("trudn") || diff == "3"
                (easy && isEasy) || (medium && isMedium) || (hard && isHard)
            }

            // 2. Diet/Categories Filter
            val vegeMatches = if (!vege) true else {
                recipe.categories?.any { cat ->
                    val c = cat.lowercase()
                    c.contains("vege") || c.contains("vegetarian") || c.contains("wegetarian")
                } ?: false
            }
            val veganMatches = if (!vegan) true else {
                recipe.categories?.any { cat ->
                    val c = cat.lowercase()
                    c.contains("vegan") || c.contains("wegańsk")
                } ?: false
            }
            val glutenFreeMatches = if (!glutenFree) true else {
                recipe.categories?.any { cat ->
                    val c = cat.lowercase()
                    c.contains("gluten") || c.contains("bezgluten")
                } ?: false
            }

            diffMatches && vegeMatches && veganMatches && glutenFreeMatches
        }
        _recipes.value = filtered
     }

    fun clearFilters() {
        filterRecipes(
            easy = false,
            medium = false,
            hard = false,
            maxTime = null,
            maxCalories = null,
            vege = false,
            vegan = false,
            glutenFree = false
        )
    }
}
