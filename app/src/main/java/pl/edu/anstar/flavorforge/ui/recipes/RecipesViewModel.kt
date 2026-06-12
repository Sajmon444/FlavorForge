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
                            valueTransform = { it.category }
                        ).mapValues { entry ->
                            entry.value.filterNotNull().map { wrapper ->
                                // POPRAWKA: Przekazujemy surowy wrapper.name (JsonElement) oraz sztuczne id (np. 0),
                                // ponieważ konstruktor Category oczekuje (id: Int, name: JsonElement, slug: String)
                                pl.edu.anstar.flavorforge.data.model.Category(
                                    id = 0,
                                    name = wrapper.name,
                                    slug = wrapper.slug
                                )
                            }
                        }

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
        val language = sessionManager.getSettingsPrefs().getString("app_lang", "pl") ?: "pl"
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
                recipe.categories?.any { category ->
                    // POPRAWKA: Pobieramy tekst z JsonElement za pomocą metody getName(language)
                    val trimmedName = category.getName(language).trim()
                    val slug = category.slug.lowercase()
                    (vege && (trimmedName.equals("Wegetariańskie", ignoreCase = true) || slug == "vegetarian")) ||
                            (vegan && (trimmedName.equals("Wegańskie", ignoreCase = true) || slug == "vegan")) ||
                            (meat && (trimmedName.equals("Mięsne", ignoreCase = true) || slug == "meat")) ||
                            (fish && (trimmedName.equals("Ryby", ignoreCase = true) || trimmedName.equals("Ryba", ignoreCase = true) || slug == "fish"))
                } ?: false
            }

            // 3. Meal Type Filter (Exact OR-Match with Polish & English fallback)
            val mealMatches = if (!breakfast && !lunch && !dinner && !dessert && !snacks) {
                true
            } else {
                recipe.categories?.any { category ->
                    // POPRAWKA: Pobieramy tekst z JsonElement za pomocą metody getName(language)
                    val trimmedName = category.getName(language).trim()
                    val slug = category.slug.lowercase()
                    (breakfast && (trimmedName.equals("Śniadanie", ignoreCase = true) || slug == "breakfast")) ||
                            (lunch && (trimmedName.equals("Obiad", ignoreCase = true) || slug == "lunch")) ||
                            (dinner && (trimmedName.equals("Kolacja", ignoreCase = true) || trimmedName.equals("Obiad", ignoreCase = true) || slug == "dinner" || slug == "lunch")) ||
                            (dessert && (trimmedName.equals("Deser", ignoreCase = true) || slug == "dessert")) ||
                            (snacks && (trimmedName.equals("Szybkie przekąski", ignoreCase = true) || trimmedName.equals("Przekąski", ignoreCase = true) || slug == "snacks"))
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