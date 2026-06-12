package pl.edu.anstar.flavorforge.data.repository

import android.util.Log
import pl.edu.anstar.flavorforge.data.model.RecipeDetails
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
        maxMissing: Int,
        language: String
    ): Result<List<RecipeSearchResult>> {
        return try {
            val request = SearchRequest(ingredients, maxMissing, language)
            val response = apiService.searchRecipes(request)

            if (response.isSuccessful && response.body() != null) {
                val searchResults = response.body()!!
                if (searchResults.isEmpty()) return Result.success(emptyList())


                val ids = searchResults.joinToString(",") { it.id.toString() }
                val detailsResponse = apiService.getRecipesByIds("in.($ids)")

                if (detailsResponse.isSuccessful && detailsResponse.body() != null) {
                    val detailsMap = detailsResponse.body()!!.associateBy { it.id }

                    val enrichedResults = searchResults.map { result ->
                        val details = detailsMap[result.id]
                        result.copy(
                            description = details?.description ?: result.description,
                            prepTime = details?.prepTime ?: result.prepTime,
                            categories = details?.categories ?: result.categories,
                            difficulty = details?.difficulty ?: result.difficulty,
                            caloriesTotal = details?.caloriesTotal ?: result.caloriesTotal
                        )
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

    override suspend fun getRecipeDetails(id: Int): Result<RecipeDetails> {
        return try {

            val selectStructure = "*,categories(*),recipe_ingredients(*,ingredients(*))"

            val response = apiService.getRecipeDetails(selectStructure, "eq.$id")

            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                val details = response.body()!!.first()
                Result.success(details)
            } else {
                Result.failure(Exception("Nie znaleziono szczegółów przepisu"))
            }
        } catch (e: Exception) {
            Log.e("RecipeRepositoryImpl", "getRecipeDetails error for id: $id", e)
            Result.failure(e)
        }
    }
}