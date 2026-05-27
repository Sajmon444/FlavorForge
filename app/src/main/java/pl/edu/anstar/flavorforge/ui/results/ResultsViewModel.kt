package pl.edu.anstar.flavorforge.ui.results

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import pl.edu.anstar.flavorforge.data.local.SessionManager
import pl.edu.anstar.flavorforge.data.model.RecipeSearchResult
import pl.edu.anstar.flavorforge.domain.usecase.SearchRecipesUseCase
import javax.inject.Inject

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val searchUseCase: SearchRecipesUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _recipes = MutableLiveData<List<RecipeSearchResult>>(emptyList())
    val recipes: LiveData<List<RecipeSearchResult>> = _recipes

    private var allRecipes: List<RecipeSearchResult> = emptyList()

    fun searchRecipes(ingredients: List<String>, maxMissing: Int) {
        viewModelScope.launch {
            searchUseCase(ingredients, maxMissing).fold(
                onSuccess = { results ->
                    allRecipes = results
                    // Initially apply settings filtering
                    clearFilters()
                },
                onFailure = { /* ignoruj błąd */ }
            )
        }
    }

    fun filterRecipes(
        easy: Boolean,
        medium: Boolean,
        hard: Boolean,
        maxTime: Int?,
        vege: Boolean,
        vegan: Boolean,
        glutenFree: Boolean
    ) {
        val sp = sessionManager.getSettingsPrefs()
        val settingsPrepProgress = sp.getInt("settings_prep_time", 12)
        val settingsMaxTime = settingsPrepProgress * 15 // min

        val settingsCaloriesProgress = sp.getInt("settings_calories", 54)
        val settingsMaxCalories = 300 + settingsCaloriesProgress * 50 // kcal

        val filtered = allRecipes.filter { recipe ->
            // --- Global settings constraints ---
            val prepTimeLimitMatches = recipe.prepTime?.let { it <= settingsMaxTime } ?: true
            val caloriesLimitMatches = recipe.caloriesTotal?.let { it <= settingsMaxCalories } ?: true

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

            // 2. Preparation Time Filter
            val timeMatches = if (maxTime == null) {
                true
            } else {
                recipe.prepTime?.let { it <= maxTime } ?: false
            }

            // 3. Diet/Categories Filter
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

            diffMatches && timeMatches && vegeMatches && veganMatches && glutenFreeMatches
        }
        _recipes.value = filtered
    }

    fun clearFilters() {
        filterRecipes(
            easy = false,
            medium = false,
            hard = false,
            maxTime = null,
            vege = false,
            vegan = false,
            glutenFree = false
        )
    }
}