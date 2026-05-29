package pl.edu.anstar.flavorforge.domain.repository

import pl.edu.anstar.flavorforge.data.model.RecipeDetails
import pl.edu.anstar.flavorforge.data.model.RecipeSearchResult

interface RecipeRepository {
    suspend fun searchRecipes(
        ingredients: List<String>,
        maxMissing: Int,
        language: String = "pl"
    ): Result<List<RecipeSearchResult>>
    suspend fun getRecipeDetails(id: Int): Result<RecipeDetails>
}
