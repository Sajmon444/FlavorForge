package pl.edu.anstar.flavorforge.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class RecipeIngredientTest {

    @Test
    fun `getUnit should return English mapping when language is English`() {
        val ingredient = RecipeIngredient(1.0, "łyżki", false, null)
        assertEquals("tablespoons", ingredient.getUnit("en"))
    }

    @Test
    fun `getUnit should return original unit when language is not English`() {
        val ingredient = RecipeIngredient(1.0, "łyżki", false, null)
        assertEquals("łyżki", ingredient.getUnit("pl"))
    }

    @Test
    fun `getUnit should return original unit if no mapping exists for English`() {
        val ingredient = RecipeIngredient(1.0, "nowa_jednostka", false, null)
        assertEquals("nowa_jednostka", ingredient.getUnit("en"))
    }

    @Test
    fun `getUnit should handle null unit`() {
        val ingredient = RecipeIngredient(1.0, null, false, null)
        assertEquals("", ingredient.getUnit("en"))
    }

    @Test
    fun `getUnit should be case insensitive`() {
        val ingredient = RecipeIngredient(1.0, "ŁYŻKA", false, null)
        assertEquals("tablespoon", ingredient.getUnit("en"))
    }

    @Test
    fun `getUnit should map all required units correctly`() {
        val units = mapOf(
            "g" to "g",
            "garść" to "handful",
            "kawałek" to "piece",
            "kg" to "kg",
            "kromki" to "slices",
            "liście" to "leaves",
            "łyżeczka" to "teaspoon",
            "łyżeczki" to "teaspoons",
            "łyżka" to "tablespoon",
            "łyżki" to "tablespoons",
            "ml" to "ml",
            "opakowanie" to "package",
            "pęczek" to "bunch",
            "plastry" to "slices",
            "puszka" to "can",
            "szczypta" to "pinch",
            "szt" to "pcs",
            "ząbek" to "clove",
            "ząbki" to "cloves"
        )

        units.forEach { (pl, en) ->
            val ingredient = RecipeIngredient(1.0, pl, false, null)
            assertEquals("Mapping failed for $pl", en, ingredient.getUnit("en"))
        }
    }
}
