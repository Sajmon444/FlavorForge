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
    @SerializedName("categories") val categories: List<Category>? = null
) {
    fun getTitle(language: String): String = parseTranslated(title, language)
    fun getDescription(language: String): String = parseTranslated(description, language)

    fun getInstructions(language: String): List<InstructionStep> {
        if (instructions == null || instructions.isJsonNull) return emptyList()
        
        return try {
            val gson = Gson()
            val listType = object : TypeToken<List<InstructionStep>>() {}.type
            
            if (instructions.isJsonArray) {
                // If it's already an array (old format)
                gson.fromJson(instructions, listType)
            } else if (instructions.isJsonObject) {
                // If it's a translated object (new format)
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
