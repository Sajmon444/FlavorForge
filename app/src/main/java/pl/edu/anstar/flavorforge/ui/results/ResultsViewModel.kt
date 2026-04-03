package pl.edu.anstar.flavorforge.ui.results

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import pl.edu.anstar.flavorforge.data.model.RecipeSearchResult
import pl.edu.anstar.flavorforge.domain.usecase.SearchRecipesUseCase
import javax.inject.Inject

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val searchUseCase: SearchRecipesUseCase
) : ViewModel() {

    private val _recipes = MutableLiveData<List<RecipeSearchResult>>(emptyList())
    val recipes: LiveData<List<RecipeSearchResult>> = _recipes

    fun searchRecipes(ingredients: List<String>) {
        viewModelScope.launch {
            searchUseCase(ingredients, 2).fold(
                onSuccess = { results -> _recipes.value = results },
                onFailure = { /* ignoruj błąd */ }
            )
        }
    }
}