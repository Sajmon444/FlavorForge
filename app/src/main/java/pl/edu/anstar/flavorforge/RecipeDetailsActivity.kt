package pl.edu.anstar.flavorforge

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.graphics.Color
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
    private lateinit var layoutCategories: android.view.View
    private lateinit var tvCategories: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_details)

        tvTitle = findViewById(R.id.tvRecipeTitle)
        ivImage = findViewById(R.id.ivRecipeImage)
        tvDescription = findViewById(R.id.tvRecipeDescription)
        tvInstructions = findViewById(R.id.tvRecipeInstructions)
        btnBack = findViewById(R.id.btnBack)
        btnFavorite = findViewById(R.id.btnFavorite)
        layoutCategories = findViewById(R.id.layoutCategories)
        tvCategories = findViewById(R.id.tvRecipeCategories)

        btnBack.setOnClickListener { finish() }

        val recipeId = intent.getIntExtra(EXTRA_RECIPE_ID, -1)
        if (recipeId != -1) {
            val isFavorite = sessionManager.isFavorite(recipeId)
            if (isFavorite) {
                btnFavorite.setColorFilter(Color.parseColor("#FF5252"))
            } else {
                btnFavorite.setColorFilter(Color.parseColor("#E8EBEE"))
            }

            btnFavorite.setOnClickListener {
                if (!sessionManager.isLoggedIn()) {
                    Toast.makeText(this, "Zaloguj się, aby zapisywać ulubione przepisy!", Toast.LENGTH_SHORT).show()
                } else {
                    val currentlyFavorite = sessionManager.isFavorite(recipeId)
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
        lifecycleScope.launch {
            repository.getRecipeDetails(id).fold(
                onSuccess = { details ->
                    displayDetails(details)
                },
                onFailure = {
                    Toast.makeText(this@RecipeDetailsActivity, "Błąd pobierania szczegółów", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun displayDetails(details: RecipeDetails) {
        tvTitle.text = details.title
        tvDescription.text = details.description

        val cats = details.categories
        if (!cats.isNullOrEmpty()) {
            layoutCategories.visibility = android.view.View.VISIBLE
            tvCategories.text = cats.joinToString(" • ")
        } else {
            layoutCategories.visibility = android.view.View.GONE
        }

        val instructionsText = details.instructions
            ?.sortedBy { it.step }
            ?.joinToString("\n\n") { "${it.step}. ${it.text}" }
            ?: "Brak instrukcji."
        tvInstructions.text = instructionsText

        Glide.with(this)
            .load(details.imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_report_image)
            .into(ivImage)
    }

    companion object {
        private const val EXTRA_RECIPE_ID = "extra_recipe_id"

        fun start(context: Context, recipeId: Int) {
            val intent = Intent(context, RecipeDetailsActivity::class.java).apply {
                putExtra(EXTRA_RECIPE_ID, recipeId)
            }
            context.startActivity(intent)
        }
    }
}
