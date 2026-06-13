package pl.edu.anstar.flavorforge

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import pl.edu.anstar.flavorforge.data.local.SessionManager
import pl.edu.anstar.flavorforge.ui.RecipesAdapter
import pl.edu.anstar.flavorforge.ui.liked.LikedViewModel
import javax.inject.Inject

@AndroidEntryPoint
class LikedActivity : AppCompatActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    private val viewModel: LikedViewModel by viewModels()

    private lateinit var rvRecipes: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var adapter: RecipesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_liked)

        rvRecipes = findViewById(R.id.rvRecipes)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)

        findViewById<ImageView>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        adapter = RecipesAdapter(sessionManager, { recipeId ->
            RecipeDetailsActivity.start(this, recipeId)
        }, { _ ->
            loadFavorites()
        })

        rvRecipes.layoutManager = LinearLayoutManager(this)
        rvRecipes.adapter = adapter

        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    private fun loadFavorites() {
        val favoriteIds = sessionManager.getFavoriteIds()
        viewModel.loadFavoriteRecipes(favoriteIds)
    }

    private fun observeViewModel() {
        viewModel.recipes.observe(this) { recipes ->
            adapter.submitList(recipes)
            // Bezpiecznik: Jeśli lista z serwera przyjdzie pusta i nie ma błędu,
            // również wyświetlamy komunikat o braku ulubionych przepisów
            if (recipes.isNullOrEmpty() && viewModel.error.value == null) {
                tvError.visibility = View.VISIBLE
                tvError.text = getString(R.string.no_liked_recipes)
            } else if (!recipes.isNullOrEmpty()) {
                tvError.visibility = View.GONE
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { errorMessage ->
            if (errorMessage != null) {
                tvError.visibility = View.VISIBLE

                // POPRAWKA: Jeśli błąd to informacja o pustej liście (niezależnie jak przekazana z VM),
                // podmieniamy ją na dynamiczny zasób językowy. W innym wypadku wyświetlamy surowy błąd sieciowy.
                if (errorMessage.contains("Brak", ignoreCase = true) || errorMessage.contains("empty", ignoreCase = true) || errorMessage.isBlank()) {
                    tvError.text = getString(R.string.no_liked_recipes)
                } else {
                    tvError.text = errorMessage
                }
            } else {
                // Jeśli błąd zniknął, ale lista dalej jest pusta, to `recipes.observe` powyżej zadba o poprawny tekst
                if (viewModel.recipes.value.isNullOrEmpty()) {
                    tvError.visibility = View.VISIBLE
                    tvError.text = getString(R.string.no_liked_recipes)
                } else {
                    tvError.visibility = View.GONE
                }
            }
        }
    }
}