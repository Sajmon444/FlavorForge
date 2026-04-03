package pl.edu.anstar.flavorforge.domain.repository

import pl.edu.anstar.flavorforge.data.model.RecipeSearchResult

interface RecipeRepository {
    suspend fun searchRecipes(ingredients: List<String>, maxMissing: Int): Result<List<RecipeSearchResult>>
}