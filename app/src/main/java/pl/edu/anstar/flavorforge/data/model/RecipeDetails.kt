package pl.edu.anstar.flavorforge.data.model

import com.google.gson.annotations.SerializedName

data class RecipeDetails(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("instructions") val instructions: List<InstructionStep>?,
    @SerializedName("prep_time") val prepTime: Int?,
    @SerializedName("difficulty") val difficulty: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("calories_total") val caloriesTotal: Int?,
    val categories: List<String>? = null
)

data class InstructionStep(
    @SerializedName("step") val step: Int,
    @SerializedName("text") val text: String
)
