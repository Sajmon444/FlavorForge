package pl.edu.anstar.flavorforge.data.model

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

data class RecipeDetails(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: JsonElement,
    @SerializedName("description") val description: JsonElement?,
    @SerializedName("instructions") val instructions: JsonElement?,
    @SerializedName("prep_time") val prepTime: Int?,
    @SerializedName("difficulty") val difficulty: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("calories_total") val caloriesTotal: Int?,
    @SerializedName("categories") val categories: List<Category>? = null,


    @SerializedName("recipe_ingredients") val recipeIngredients: List<RecipeIngredient>? = null
) {
    fun getTitle(language: String): String = parseTranslated(title, language)
    fun getDescription(language: String): String = parseTranslated(description, language)

    fun getInstructions(language: String): List<InstructionStep> {
        if (instructions == null || instructions.isJsonNull) return emptyList()

        return try {
            val gson = Gson()
            val listType = object : TypeToken<List<InstructionStep>>() {}.type

            if (instructions.isJsonArray) {
                gson.fromJson(instructions, listType)
            } else if (instructions.isJsonObject) {
                val obj = instructions.asJsonObject
                val langElement = if (obj.has(language)) {
                    obj.get(language)
                } else {
                    obj.get("en") ?: obj.get("pl")
                }

                if (langElement != null && langElement.isJsonArray) {
                    gson.fromJson(langElement, listType)
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

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

// Model reprezentujący wiersz z tabeli recipe_ingredients
data class RecipeIngredient(
    @SerializedName("quantity") val quantity: Double,
    @SerializedName("unit") val unit: String?,
    @SerializedName("is_optional") val isOptional: Boolean,

    @SerializedName("ingredients") val ingredientDetails: IngredientInnerDetails?
) {
    fun getUnit(language: String): String {
        if (unit == null) return ""
        if (language != "en") return unit

        return when (unit.lowercase()) {
            "g" -> "g"
            "garść" -> "handful"
            "kawałek" -> "piece"
            "kg" -> "kg"
            "kromki" -> "slices"
            "liście" -> "leaves"
            "łyżeczka" -> "teaspoon"
            "łyżeczki" -> "teaspoons"
            "łyżka" -> "tablespoon"
            "łyżki" -> "tablespoons"
            "ml" -> "ml"
            "opakowanie" -> "package"
            "pęczek" -> "bunch"
            "plastry" -> "slices"
            "puszka" -> "can"
            "szczypta" -> "pinch"
            "szt" -> "pcs"
            "ząbek" -> "clove"
            "ząbki" -> "cloves"
            else -> unit
        }
    }
}


data class IngredientInnerDetails(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: JsonElement
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