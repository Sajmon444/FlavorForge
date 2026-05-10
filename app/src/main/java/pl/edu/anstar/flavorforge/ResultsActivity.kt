package pl.edu.anstar.flavorforge

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import pl.edu.anstar.flavorforge.ui.RecipesAdapter
import pl.edu.anstar.flavorforge.ui.results.ResultsViewModel

@AndroidEntryPoint
class ResultsActivity : AppCompatActivity() {

    private val viewModel: ResultsViewModel by viewModels()
    private lateinit var rvRecipes: RecyclerView
    private lateinit var adapter: RecipesAdapter
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        drawerLayout = findViewById(R.id.drawer_layout_results)

        findViewById<ImageView>(R.id.btnBack)?.setOnClickListener {
            finish()
        }


        findViewById<ImageView>(R.id.btnFilter)?.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        // Obsługa przycisków wewnątrz drawera
        findViewById<Button>(R.id.btnApplyFilters)?.setOnClickListener {

            drawerLayout.closeDrawer(GravityCompat.END)
        }

        findViewById<Button>(R.id.btnClearFilters)?.setOnClickListener {

            drawerLayout.closeDrawer(GravityCompat.END)
        }

        rvRecipes = findViewById(R.id.rvRecipes)
        adapter = RecipesAdapter { recipeId ->
            RecipeDetailsActivity.start(this, recipeId)
        }
        rvRecipes.layoutManager = LinearLayoutManager(this)
        rvRecipes.adapter = adapter

        val ingredients = intent.getStringArrayListExtra(EXTRA_INGREDIENTS) ?: emptyList()
        viewModel.searchRecipes(ingredients)

        viewModel.recipes.observe(this) { recipes ->
            adapter.submitList(recipes)
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val EXTRA_INGREDIENTS = "extra_ingredients"

        fun start(context: Context, ingredients: List<String>) {
            val intent = Intent(context, ResultsActivity::class.java).apply {
                putStringArrayListExtra(EXTRA_INGREDIENTS, ArrayList(ingredients))
            }
            context.startActivity(intent)
        }
    }
}
