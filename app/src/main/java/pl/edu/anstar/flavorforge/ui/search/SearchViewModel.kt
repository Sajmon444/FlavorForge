package pl.edu.anstar.flavorforge.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor() : ViewModel() {

    private val _ingredients = MutableLiveData<List<String>>(emptyList())
    val ingredients: LiveData<List<String>> = _ingredients

    fun addIngredient(ingredient: String) {
        if (ingredient.isNotBlank()) {
            val current = _ingredients.value ?: emptyList()
            if (ingredient !in current) {
                _ingredients.value = current + ingredient
            }
        }
    }

    fun removeIngredient(ingredient: String) {
        val current = _ingredients.value ?: emptyList()
        _ingredients.value = current - ingredient
    }
}