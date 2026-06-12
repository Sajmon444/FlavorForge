package pl.edu.anstar.flavorforge.data.remote

import pl.edu.anstar.flavorforge.data.model.RecipeDetails
import pl.edu.anstar.flavorforge.data.model.RecipeSearchResult
import pl.edu.anstar.flavorforge.data.model.SearchRequest
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query

interface RecipeApiService {
    @POST("/rest/v1/rpc/search_recipes")
    suspend fun searchRecipes(@Body request: SearchRequest): Response<List<RecipeSearchResult>>

    @GET("/rest/v1/recipes")
    suspend fun getRecipesByIds(@Query("id") idFilter: String): Response<List<RecipeSearchResult>>


    @GET("/rest/v1/recipes")
    suspend fun getRecipeDetails(
        @Query("select") select: String,
        @Query("id") idFilter: String
    ): Response<List<RecipeDetails>>

    @GET("/rest/v1/recipe_categories?select=recipe_id,categories(name,slug)")
    suspend fun getAllRecipeCategories(): Response<List<RecipeCategoryMapping>>
}

data class RecipeCategoryMapping(
    @SerializedName("recipe_id") val recipeId: Int,
    @SerializedName("categories") val category: CategoryInfoWrapper?
)

data class CategoryInfoWrapper(
    @SerializedName("name") val name: JsonElement,
    @SerializedName("slug") val slug: String
)