package pl.edu.anstar.flavorforge.data.repository

import pl.edu.anstar.flavorforge.data.model.RecipeSearchResult
import pl.edu.anstar.flavorforge.data.model.SearchRequest
import pl.edu.anstar.flavorforge.data.remote.RecipeApiService
import pl.edu.anstar.flavorforge.domain.repository.RecipeRepository
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val apiService: RecipeApiService
) : RecipeRepository {

    override suspend fun searchRecipes(
        ingredients: List<String>,
        maxMissing: Int
    ): Result<List<RecipeSearchResult>> {
        return try {
            val request = SearchRequest(ingredients, maxMissing)
            val response = apiService.searchRecipes(request)

            if (response.isSuccessful && response.body() != null) {
                val searchResults = response.body()!!
                if (searchResults.isEmpty()) return Result.success(emptyList())

                val ids = searchResults.joinToString(",") { it.id.toString() }
                val detailsResponse = apiService.getRecipesByIds("in.($ids)")

                if (detailsResponse.isSuccessful && detailsResponse.body() != null) {
                    val detailsMap = detailsResponse.body()!!.associateBy { it.id }

                    val enrichedResults = searchResults.map { result ->
                        result.copy(description = detailsMap[result.id]?.description)
                    }
                    Result.success(enrichedResults)
                } else {
                    Result.success(searchResults)
                }
            } else {
                Result.failure(Exception("Błąd API"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}