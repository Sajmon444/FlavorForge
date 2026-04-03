package pl.edu.anstar.flavorforge.domain.usecase

import pl.edu.anstar.flavorforge.data.model.RecipeSearchResult
import pl.edu.anstar.flavorforge.domain.repository.RecipeRepository
import javax.inject.Inject

class SearchRecipesUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(
        ingredients: List<String>,
        maxMissing: Int = 2
    ): Result<List<RecipeSearchResult>> {
        return repository.searchRecipes(ingredients, maxMissing)
    }
}