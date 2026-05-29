package pl.edu.anstar.flavorforge.data.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class RecipeDetails(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: JsonElement,
    @SerializedName("description") val description: JsonElement?,
    @SerializedName("instructions") val instructions: List<InstructionStep>?,
    @SerializedName("prep_time") val prepTime: Int?,
    @SerializedName("difficulty") val difficulty: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("calories_total") val caloriesTotal: Int?,
    @SerializedName("categories") val categories: List<Category>? = null
) {
    fun getTitle(language: String): String = parseTranslated(title, language)
    fun getDescription(language: String): String = parseTranslated(description, language)

    private fun parseTranslated(element: JsonElement?, language: String): String {
        if (element == null || element.isJsonNull) return ""
        if (element.isJsonPrimitive) return element.asString
        return try {
            val obj = element.asJsonObject
            if (obj.has(language)) obj.get(language).asString
            else obj.get("en")?.asString ?: obj.get("pl")?.asString ?: ""
        } catch (e: Exception) { "" }
    }
}

data class InstructionStep(
    @SerializedName("step") val step: Int,
    @SerializedName("text") val text: JsonElement
) {
    fun getText(language: String): String {
        if (text.isJsonPrimitive) return text.asString
        return try {
            val obj = text.asJsonObject
            if (obj.has(language)) obj.get(language).asString
            else obj.get("en")?.asString ?: obj.get("pl")?.asString ?: ""
        } catch (e: Exception) { "" }
    }
}
