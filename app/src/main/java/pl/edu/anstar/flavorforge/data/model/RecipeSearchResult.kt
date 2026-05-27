package pl.edu.anstar.flavorforge.data.model

import com.google.gson.annotations.SerializedName

data class RecipeSearchResult(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("prep_time") val prepTime: Int?,
    @SerializedName("categories") val categories: List<String>?,
    @SerializedName("match_score") val matchScore: Int?,
    @SerializedName("missing_count") val missingCount: Int?,
    @SerializedName("matched_ingredients") val matchedIngredients: String?,
    @SerializedName("missing_ingredients") val missingIngredients: String?,
    @SerializedName("difficulty") val difficulty: String? = null,
    @SerializedName("calories_total") val caloriesTotal: Int? = null
)