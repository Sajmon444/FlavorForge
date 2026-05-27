package pl.edu.anstar.flavorforge

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import pl.edu.anstar.flavorforge.data.local.SessionManager
import pl.edu.anstar.flavorforge.ui.RecipesAdapter
import pl.edu.anstar.flavorforge.ui.recipes.RecipesViewModel
import javax.inject.Inject

@AndroidEntryPoint
class RecipesActivity : AppCompatActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

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
            applyCurrentFilters()
            drawerLayout.closeDrawer(GravityCompat.END)
        }

        findViewById<Button>(R.id.btnClearFilters)?.setOnClickListener {
            findViewById<CheckBox>(R.id.cbEasy)?.isChecked = false
            findViewById<CheckBox>(R.id.cbMedium)?.isChecked = false
            findViewById<CheckBox>(R.id.cbHard)?.isChecked = false
            findViewById<RadioGroup>(R.id.rgPreparationTime)?.check(R.id.rbTimeAny)
            findViewById<CheckBox>(R.id.cbVege)?.isChecked = false
            findViewById<CheckBox>(R.id.cbVegan)?.isChecked = false
            findViewById<CheckBox>(R.id.cbGlutenFree)?.isChecked = false
            viewModel.clearFilters()
            drawerLayout.closeDrawer(GravityCompat.END)
        }

        adapter = RecipesAdapter(sessionManager, { recipeId ->
            RecipeDetailsActivity.start(this, recipeId)
        })
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

    override fun onResume() {
        super.onResume()
        applyCurrentFilters()
    }

    private fun applyCurrentFilters() {
        val cbEasy = findViewById<CheckBox>(R.id.cbEasy)
        val cbMedium = findViewById<CheckBox>(R.id.cbMedium)
        val cbHard = findViewById<CheckBox>(R.id.cbHard)
        val rgPreparationTime = findViewById<RadioGroup>(R.id.rgPreparationTime)
        val cbVege = findViewById<CheckBox>(R.id.cbVege)
        val cbVegan = findViewById<CheckBox>(R.id.cbVegan)
        val cbGlutenFree = findViewById<CheckBox>(R.id.cbGlutenFree)

        val maxTime = when (rgPreparationTime?.checkedRadioButtonId) {
            R.id.rbTime30 -> 30
            R.id.rbTime60 -> 60
            else -> null
        }
        viewModel.filterRecipes(
            easy = cbEasy?.isChecked ?: false,
            medium = cbMedium?.isChecked ?: false,
            hard = cbHard?.isChecked ?: false,
            maxTime = maxTime,
            vege = cbVege?.isChecked ?: false,
            vegan = cbVegan?.isChecked ?: false,
            glutenFree = cbGlutenFree?.isChecked ?: false
        )
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }
}
