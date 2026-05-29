package pl.edu.anstar.flavorforge.data.model

import com.google.gson.annotations.SerializedName

data class TranslatedString(
    @SerializedName("pl") val pl: String?,
    @SerializedName("en") val en: String?
) {
    fun get(language: String): String {
        return when (language) {
            "pl" -> pl ?: en ?: ""
            "en" -> en ?: pl ?: ""
            else -> en ?: pl ?: ""
        }
    }
}
