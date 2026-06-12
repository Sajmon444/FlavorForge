package pl.edu.anstar.flavorforge.data.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class RecipeSearchResult(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: JsonElement,
    @SerializedName("description") val description: JsonElement?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("prep_time") val prepTime: Int?,
    @SerializedName("categories") val categories: List<Category>?,
    @SerializedName("category_slugs") val categorySlugs: List<String>?,
    @SerializedName("match_score") val matchScore: Int?,
    @SerializedName("missing_count") val missingCount: Int?,
    @SerializedName("matched_ingredients") val matchedIngredients: String?,
    @SerializedName("missing_ingredients") val missingIngredients: String?,
    @SerializedName("difficulty") val difficulty: String? = null,
    @SerializedName("calories_total") val caloriesTotal: Int? = null
) {
    fun getTitle(language: String): String {
        return parseTranslated(title, language)
    }

    fun getDescription(language: String): String {
        return parseTranslated(description, language)
    }

    private fun parseTranslated(element: JsonElement?, language: String): String {
        if (element == null || element.isJsonNull) return ""
        if (element.isJsonPrimitive) return element.asString
        
        return try {
            val obj = element.asJsonObject
            val text = if (obj.has(language)) {
                obj.get(language).asString
            } else {
                obj.get("en")?.asString ?: obj.get("pl")?.asString ?: ""
            }
            text
        } catch (e: Exception) {
            ""
        }
    }
}

data class Category(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: JsonElement,
    @SerializedName("slug") val slug: String
) {

    fun getName(language: String): String {
        if (name.isJsonNull) return ""
        if (name.isJsonPrimitive) return name.asString
        return try {
            val obj = name.asJsonObject
            if (obj.has(language)) obj.get(language).asString
            else obj.get("en")?.asString ?: obj.get("pl")?.asString ?: ""
        } catch (e: Exception) { "" }
    }
}