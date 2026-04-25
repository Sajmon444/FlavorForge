package pl.edu.anstar.flavorforge

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import pl.edu.anstar.flavorforge.ui.RecipesAdapter
import pl.edu.anstar.flavorforge.ui.recipes.RecipesViewModel

@AndroidEntryPoint
class RecipesActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var rvRecipes: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var adapter: RecipesAdapter

    private val viewModel: RecipesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recepies)

        drawerLayout = findViewById(R.id.drawerLayout)
        rvRecipes = findViewById(R.id.rvRecipes)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)

        findViewById<ImageView>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.btnFilters)?.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        findViewById<Button>(R.id.btnApplyFilters)?.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
        }

        adapter = RecipesAdapter { recipeId ->
            RecipeDetailsActivity.start(this, recipeId)
        }
        rvRecipes.adapter = adapter

        observeViewModel()
        
        progressBar.visibility = View.VISIBLE
        viewModel.fetchLatestRecipes()
    }

    private fun observeViewModel() {
        viewModel.recipes.observe(this) { recipes ->
            progressBar.visibility = View.GONE
            adapter.submitList(recipes)
            tvError.visibility = if (recipes.isEmpty()) View.VISIBLE else View.GONE
            if (recipes.isEmpty()) {
                tvError.text = "Brak przepisów."
            }
        }

        viewModel.error.observe(this) { errorMessage ->
            progressBar.visibility = View.GONE
            tvError.visibility = View.VISIBLE
            tvError.text = errorMessage
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }
}
