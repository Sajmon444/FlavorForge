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

        // Initialize RecipesAdapter with dynamic favorites click callback
        adapter = RecipesAdapter(sessionManager, { recipeId ->
            RecipeDetailsActivity.start(this, recipeId)
        }, { _ ->
            // Instantly refresh list when unfavoriting from list
            loadFavorites()
        })

        rvRecipes.layoutManager = LinearLayoutManager(this)
        rvRecipes.adapter = adapter

        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // Refresh every time screen becomes active (syncs changes from RecipeDetailsActivity)
        loadFavorites()
    }

    private fun loadFavorites() {
        val favoriteIds = sessionManager.getFavoriteIds()
        viewModel.loadFavoriteRecipes(favoriteIds)
    }

    private fun observeViewModel() {
        viewModel.recipes.observe(this) { recipes ->
            adapter.submitList(recipes)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { errorMessage ->
            if (errorMessage != null) {
                tvError.visibility = View.VISIBLE
                tvError.text = errorMessage
            } else {
                tvError.visibility = View.GONE
            }
        }
    }
}
