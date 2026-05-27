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
                    val recipesList = response.body()!!
                    
                    // Fetch category mappings and enrich recipes list
                    val mappingsResponse = apiService.getAllRecipeCategories()
                    val enrichedRecipes = if (mappingsResponse.isSuccessful && mappingsResponse.body() != null) {
                        val mappings = mappingsResponse.body()!!
                        val categoryMap = mappings.groupBy(
                            keySelector = { it.recipeId },
                            valueTransform = { it.category?.name }
                        ).mapValues { entry -> entry.value.filterNotNull() }
                        
                        recipesList.map { recipe ->
                            recipe.copy(categories = categoryMap[recipe.id] ?: emptyList())
                        }
                    } else {
                        recipesList
                    }
                    
                    allRecipes = enrichedRecipes
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
        meat: Boolean,
        fish: Boolean,
        breakfast: Boolean,
        lunch: Boolean,
        dinner: Boolean,
        dessert: Boolean,
        snacks: Boolean
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

            // 2. Diet Filter (Exact Match with Polish & English fallback)
            val dietMatches = if (!vege && !vegan && !meat && !fish) {
                true
            } else {
                recipe.categories?.any { cat ->
                    val trimmedCat = cat.trim()
                    (vege && (trimmedCat.equals("Wegetariańskie", ignoreCase = true) || trimmedCat.equals("vegetarian", ignoreCase = true))) ||
                    (vegan && (trimmedCat.equals("Wegańskie", ignoreCase = true) || trimmedCat.equals("vegan", ignoreCase = true))) ||
                    (meat && (trimmedCat.equals("Mięsne", ignoreCase = true) || trimmedCat.equals("meat", ignoreCase = true))) ||
                    (fish && (trimmedCat.equals("Ryby", ignoreCase = true) || trimmedCat.equals("Ryba", ignoreCase = true) || trimmedCat.equals("fish", ignoreCase = true)))
                } ?: false
            }

            // 3. Meal Type Filter (Exact OR-Match with Polish & English fallback)
            // LUNCH is cbMealLunch (text: "Lunch"). LUNCH in DB matches "Obiad" (slug "lunch").
            // DINNER is cbMealDinner (text: "Obiad"). DINNER in DB matches "Kolacja" (slug "dinner").
            // So if dinner is checked, we also match "Obiad"! If lunch is checked, we also match "Obiad"!
            val mealMatches = if (!breakfast && !lunch && !dinner && !dessert && !snacks) {
                true
            } else {
                recipe.categories?.any { cat ->
                    val trimmedCat = cat.trim()
                    (breakfast && (trimmedCat.equals("Śniadanie", ignoreCase = true) || trimmedCat.equals("breakfast", ignoreCase = true))) ||
                    (lunch && (trimmedCat.equals("Obiad", ignoreCase = true) || trimmedCat.equals("lunch", ignoreCase = true))) ||
                    (dinner && (trimmedCat.equals("Kolacja", ignoreCase = true) || trimmedCat.equals("Obiad", ignoreCase = true) || trimmedCat.equals("dinner", ignoreCase = true) || trimmedCat.equals("lunch", ignoreCase = true))) ||
                    (dessert && (trimmedCat.equals("Deser", ignoreCase = true) || trimmedCat.equals("dessert", ignoreCase = true))) ||
                    (snacks && (trimmedCat.equals("Szybkie przekąski", ignoreCase = true) || trimmedCat.equals("Przekąski", ignoreCase = true) || trimmedCat.equals("snacks", ignoreCase = true)))
                } ?: false
            }

            diffMatches && dietMatches && mealMatches
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
            meat = false,
            fish = false,
            breakfast = false,
            lunch = false,
            dinner = false,
            dessert = false,
            snacks = false
        )
    }
}
