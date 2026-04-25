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
import pl.edu.anstar.flavorforge.data.model.RecipeDetails
import pl.edu.anstar.flavorforge.domain.repository.RecipeRepository
import javax.inject.Inject

@AndroidEntryPoint
class RecipeDetailsActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: RecipeRepository

    private lateinit var tvTitle: TextView
    private lateinit var ivImage: ImageView
    private lateinit var tvDescription: TextView
    private lateinit var tvInstructions: TextView
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_details)

        tvTitle = findViewById(R.id.tvRecipeTitle)
        ivImage = findViewById(R.id.ivRecipeImage)
        tvDescription = findViewById(R.id.tvRecipeDescription)
        tvInstructions = findViewById(R.id.tvRecipeInstructions)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        val recipeId = intent.getIntExtra(EXTRA_RECIPE_ID, -1)
        if (recipeId != -1) {
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
