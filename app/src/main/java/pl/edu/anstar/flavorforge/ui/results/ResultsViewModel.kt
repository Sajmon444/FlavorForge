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

    var isRecipesLoaded = false
        private set

    fun searchRecipes(ingredients: List<String>, maxMissing: Int, onComplete: () -> Unit = { clearFilters() }) {
        isRecipesLoaded = false
        val language = java.util.Locale.getDefault().language.let {
            if (it == "pl" || it == "en") it else "en"
        }
        viewModelScope.launch {
            searchUseCase(ingredients, maxMissing, language).fold(
                onSuccess = { results ->
                    allRecipes = results
                    isRecipesLoaded = true
                    onComplete()
                },
                onFailure = { 
                    isRecipesLoaded = true
                    /* ignoruj błąd */ 
                }
            )
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
        if (!isRecipesLoaded) return
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
                recipe.categories?.any { rawCategory ->
                    // ROZWIĄZANIE: Rzutujemy obiekt na właściwą klasę Category z pl.edu.anstar.flavorforge.data.model
                    val category = rawCategory as? pl.edu.anstar.flavorforge.data.model.Category
                    if (category != null) {
                        val trimmedName = category.getName(language).trim()
                        val slug = category.slug.lowercase()
                        (vege && (trimmedName.equals("Wegetariańskie", ignoreCase = true) || slug == "vegetarian")) ||
                                (vegan && (trimmedName.equals("Wegańskie", ignoreCase = true) || slug == "vegan")) ||
                                (meat && (trimmedName.equals("Mięsne", ignoreCase = true) || slug == "meat")) ||
                                (fish && (trimmedName.equals("Ryby", ignoreCase = true) || trimmedName.equals("Ryba", ignoreCase = true) || slug == "fish"))
                    } else false
                } ?: false
            }

            // 3. Meal Type Filter (Exact OR-Match with Polish & English fallback)
            val mealMatches = if (!breakfast && !lunch && !dinner && !dessert && !snacks) {
                true
            } else {
                recipe.categories?.any { rawCategory ->
                    // ROZWIĄZANIE: Rzutujemy obiekt na właściwą klasę Category z pl.edu.anstar.flavorforge.data.model
                    val category = rawCategory as? pl.edu.anstar.flavorforge.data.model.Category
                    if (category != null) {
                        val trimmedName = category.getName(language).trim()
                        val slug = category.slug.lowercase()
                        (breakfast && (trimmedName.equals("Śniadanie", ignoreCase = true) || slug == "breakfast")) ||
                                (lunch && (trimmedName.equals("Obiad", ignoreCase = true) || slug == "lunch")) ||
                                (dinner && (trimmedName.equals("Kolacja", ignoreCase = true) || trimmedName.equals("Obiad", ignoreCase = true) || slug == "dinner" || slug == "lunch")) ||
                                (dessert && (trimmedName.equals("Deser", ignoreCase = true) || slug == "dessert")) ||
                                (snacks && (trimmedName.equals("Szybkie przekąski", ignoreCase = true) || trimmedName.equals("Przekąski", ignoreCase = true) || slug == "snacks"))
                    } else false
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
    companion object {
        private val ingredientCategories = mapOf(
            "jajka" to "nabiał",
            "mąka pszenna" to "produkty zbożowe",
            "cukier" to "produkty spożywcze",
            "mleko" to "nabiał",
            "pomidor" to "warzywa",
            "makaron" to "produkty zbożowe",
            "ser żółty" to "nabiał",
            "bazylia" to "przyprawy",
            "masło" to "nabiał",
            "woda" to "napoje",
            "cebula" to "warzywa",
            "czosnek" to "warzywa",
            "sól" to "przyprawy",
            "pieprz czarny" to "przyprawy",
            "ziemniaki" to "warzywa",
            "kurczak" to "mięso",
            "łosoś" to "ryba",
            "śmietana" to "nabiał",
            "twaróg" to "nabiał",
            "szczypiorek" to "warzywa",
            "oliwa z oliwek" to "tłuszcze",
            "majonez" to "produkty spożywcze",
            "szynka" to "mięso",
            "ogórek" to "warzywa",
            "ryż" to "produkty zbożowe",
            "cukinia" to "warzywa",
            "mozzarella" to "nabiał",
            "tuńczyk w puszce" to "ryba",
            "kukurydza konserwowa" to "warzywa",
            "awokado" to "warzywa",
            "kabanosy" to "mięso",
            "oliwki" to "warzywa",
            "pesto bazyliowe" to "produkty spożywcze",
            "jabłka" to "owoce",
            "cynamon" to "przyprawy",
            "kasza kuskus" to "produkty zbożowe",
            "ser feta" to "nabiał",
            "pomidorki koktajlowe" to "warzywa",
            "mleczko kokosowe" to "produkty spożywcze",
            "curry" to "przyprawy",
            "ciasto francuskie" to "produkty zbożowe",
            "sos bbq" to "produkty spożywcze",
            "miód" to "produkty spożywcze",
            "tortilla" to "produkty zbożowe",
            "mieszanka warzyw mrożonych" to "warzywa",
            "sos sojowy" to "produkty spożywcze",
            "koper" to "przyprawy",
            "chleb" to "produkty zbożowe",
            "sok z cytryny" to "produkty spożywcze",
            "jogurt naturalny" to "nabiał"
        )
    }
}