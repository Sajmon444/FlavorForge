package pl.edu.anstar.flavorforge.data.remote

import pl.edu.anstar.flavorforge.data.model.RecipeSearchResult
import pl.edu.anstar.flavorforge.data.model.SearchRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RecipeApiService {
    @POST("rpc/search_recipes")
    suspend fun searchRecipes(@Body request: SearchRequest): Response<List<RecipeSearchResult>>
}