package pl.edu.anstar.flavorforge

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import pl.edu.anstar.flavorforge.data.local.SessionManager
import pl.edu.anstar.flavorforge.data.model.RecipeDetails
import pl.edu.anstar.flavorforge.domain.repository.RecipeRepository
import javax.inject.Inject

@AndroidEntryPoint
class RecipeDetailsActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: RecipeRepository

    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var tvTitle: TextView
    private lateinit var ivImage: ImageView
    private lateinit var tvDescription: TextView
    private lateinit var tvInstructions: TextView
    private lateinit var btnBack: ImageView
    private lateinit var btnFavorite: ImageView
    private lateinit var layoutCategories: View
    private lateinit var tvCategories: TextView
    private lateinit var layoutIngredients: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_details)
        Log.d(TAG, "onCreate: Activity started")

        tvTitle = findViewById(R.id.tvRecipeTitle)
        ivImage = findViewById(R.id.ivRecipeImage)
        tvDescription = findViewById(R.id.tvRecipeDescription)
        tvInstructions = findViewById(R.id.tvRecipeInstructions)
        btnBack = findViewById(R.id.btnBack)
        btnFavorite = findViewById(R.id.btnFavorite)
        layoutCategories = findViewById(R.id.layoutCategories)
        tvCategories = findViewById(R.id.tvRecipeCategories)
        layoutIngredients = findViewById(R.id.layoutIngredients)

        btnBack.setOnClickListener {
            Log.d(TAG, "btnBack: Back button clicked")
            finish()
        }

        val recipeId = intent.getIntExtra(EXTRA_RECIPE_ID, -1)
        Log.d(TAG, "onCreate: recipeId = $recipeId")
        if (recipeId != -1) {
            val isFavorite = sessionManager.isFavorite(recipeId)
            if (isFavorite) {
                btnFavorite.setColorFilter(Color.parseColor("#FF5252"))
            } else {
                btnFavorite.setColorFilter(Color.parseColor("#E8EBEE"))
            }

            btnFavorite.setOnClickListener {
                Log.d(TAG, "btnFavorite: Clicked")
                if (!sessionManager.isLoggedIn()) {
                    Log.d(TAG, "btnFavorite: User not logged in")
                    Toast.makeText(this, "Zaloguj się, aby zapisywać ulubione przepisy!", Toast.LENGTH_SHORT).show()
                } else {
                    val currentlyFavorite = sessionManager.isFavorite(recipeId)
                    Log.d(TAG, "btnFavorite: currentlyFavorite = $currentlyFavorite")
                    if (currentlyFavorite) {
                        sessionManager.removeFavorite(recipeId)
                        btnFavorite.setColorFilter(Color.parseColor("#E8EBEE"))
                        Toast.makeText(this, "Usunięto z ulubionych", Toast.LENGTH_SHORT).show()
                    } else {
                        sessionManager.addFavorite(recipeId)
                        btnFavorite.setColorFilter(Color.parseColor("#FF5252"))
                        Toast.makeText(this, "Dodano do ulubionych", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            loadRecipeDetails(recipeId)
        } else {
            Toast.makeText(this, "Błąd: Brak ID przepisu", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadRecipeDetails(id: Int) {
        Log.d(TAG, "loadRecipeDetails: Loading recipe with id = $id")
        lifecycleScope.launch {
            repository.getRecipeDetails(id).fold(
                onSuccess = { details ->
                    Log.d(TAG, "loadRecipeDetails: Success")
                    displayDetails(details)
                },
                onFailure = {
                    Log.e(TAG, "loadRecipeDetails: Failure", it)
                    Toast.makeText(this@RecipeDetailsActivity, "Błąd pobierania szczegółów", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun displayDetails(details: RecipeDetails) {
        Log.d(TAG, "displayDetails: Displaying details for recipe: ${details.id}")
        val language = sessionManager.getSettingsPrefs().getString("app_lang", "pl") ?: "pl"

        tvTitle.text = details.getTitle(language)
        tvDescription.text = details.getDescription(language)


        val cats = details.categories
        if (!cats.isNullOrEmpty()) {
            layoutCategories.visibility = View.VISIBLE
            tvCategories.text = cats.joinToString(" • ") { it.getName(language) }
        } else {
            layoutCategories.visibility = View.GONE
        }

        // Obsługa Składników
        layoutIngredients.removeAllViews()
        val ingrs = details.recipeIngredients

        if (!ingrs.isNullOrEmpty()) {
            ingrs.forEach { recipeIngredient ->
                val ingredientName = recipeIngredient.ingredientDetails?.getName(language) ?: ""

                if (ingredientName.isNotEmpty()) {
                    val textView = TextView(this).apply {

                        val formatAmount = if (recipeIngredient.quantity > 0) {
                            val amountInt = recipeIngredient.quantity.toInt()
                            if (recipeIngredient.quantity == amountInt.toDouble()) "$amountInt " else "${recipeIngredient.quantity} "
                        } else ""

                        val unitText = if (!recipeIngredient.unit.isNullOrEmpty()) "${recipeIngredient.unit} " else ""

                        // Opcjonalny dopisek, jeśli składnik nie jest wymagany
                        val optionalText = if (recipeIngredient.isOptional) " (opcjonalnie)" else ""

                        text = "• $formatAmount$unitText$ingredientName$optionalText"
                        textSize = 16f
                        setTextColor(Color.parseColor("#E8EBEE"))
                        setPadding(0, 6.toPx(), 0, 6.toPx())
                    }
                    layoutIngredients.addView(textView)
                }
            }
        } else {
            val emptyTextView = TextView(this).apply {
                text = "Brak składników dla tego przepisu."
                textSize = 14f
                setTextColor(Color.GRAY)
                setPadding(0, 6.toPx(), 0, 6.toPx())
            }
            layoutIngredients.addView(emptyTextView)
        }

        // Obsługa Instrukcji
        val instructionsText = details.getInstructions(language)
            .sortedBy { it.step }
            .joinToString("\n\n") { "${it.step}. ${it.getText(language)}" }
            .ifEmpty { "Brak instrukcji." }
        tvInstructions.text = instructionsText

        // Obsługa obrazka za pomocą Glide
        Glide.with(this)
            .load(details.imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_report_image)
            .into(ivImage)
    }

    private fun Int.toPx(): Int = (this * resources.displayMetrics.density).toInt()

    companion object {
        private const val TAG = "RecipeDetailsActivity"
        private const val EXTRA_RECIPE_ID = "extra_recipe_id"

        fun start(context: Context, recipeId: Int) {
            Log.d(TAG, "start: Starting activity with recipeId = $recipeId")
            val intent = Intent(context, RecipeDetailsActivity::class.java).apply {
                putExtra(EXTRA_RECIPE_ID, recipeId)
            }
            context.startActivity(intent)
        }
    }
}