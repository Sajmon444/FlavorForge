package pl.edu.anstar.flavorforge.data.model

import com.google.gson.annotations.SerializedName

data class SearchRequest(
    @SerializedName("p_user_ingredients") val userIngredients: List<String>,
    @SerializedName("p_max_missing") val maxMissing: Int = 2,
    @SerializedName("p_language") val language: String = "pl"
)