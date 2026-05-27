package pl.edu.anstar.flavorforge

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import pl.edu.anstar.flavorforge.data.local.SessionManager
import pl.edu.anstar.flavorforge.ui.RecipesAdapter
import pl.edu.anstar.flavorforge.ui.results.ResultsViewModel
import javax.inject.Inject

@AndroidEntryPoint
class ResultsActivity : AppCompatActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    private val viewModel: ResultsViewModel by viewModels()
    private lateinit var rvRecipes: RecyclerView
    private lateinit var adapter: RecipesAdapter
    private lateinit var drawerLayout: DrawerLayout

    private var activeDrawerMaxTime: Int? = null
    private var activeDrawerMaxCalories: Int? = null

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

        val cbEasy = findViewById<CheckBox>(R.id.cbEasy)
        val cbMedium = findViewById<CheckBox>(R.id.cbMedium)
        val cbHard = findViewById<CheckBox>(R.id.cbHard)

        val sbFilterTime = findViewById<SeekBar>(R.id.sbFilterTime)
        val tvFilterTimeLabel = findViewById<TextView>(R.id.tvFilterTimeLabel)
        val sbFilterCalories = findViewById<SeekBar>(R.id.sbFilterCalories)
        val tvFilterCaloriesLabel = findViewById<TextView>(R.id.tvFilterCaloriesLabel)

        // Initialize SeekBars and set up listeners
        sbFilterTime?.let {
            val minutes = it.progress * 15
            tvFilterTimeLabel?.text = getString(R.string.settings_time_label, minutes)
            it.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val currentMinutes = progress * 15
                    tvFilterTimeLabel?.text = getString(R.string.settings_time_label, currentMinutes)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        sbFilterCalories?.let {
            val kcal = 300 + it.progress * 50
            tvFilterCaloriesLabel?.text = getString(R.string.settings_calories, kcal)
            it.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val currentKcal = 300 + progress * 50
                    tvFilterCaloriesLabel?.text = getString(R.string.settings_calories, currentKcal)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        // Obsługa przycisków wewnątrz drawera
        findViewById<Button>(R.id.btnApplyFilters)?.setOnClickListener {
            activeDrawerMaxTime = sbFilterTime?.progress?.let { it * 15 }
            activeDrawerMaxCalories = sbFilterCalories?.progress?.let { 300 + it * 50 }
            applyCurrentFilters()
            drawerLayout.closeDrawer(GravityCompat.END)
        }

        findViewById<Button>(R.id.btnClearFilters)?.setOnClickListener {
            cbEasy?.isChecked = false
            cbMedium?.isChecked = false
            cbHard?.isChecked = false
            findViewById<RadioGroup>(R.id.rgDiet)?.check(R.id.rbDietAny)

            sbFilterTime?.progress = 12
            sbFilterCalories?.progress = 54
            activeDrawerMaxTime = null
            activeDrawerMaxCalories = null

            viewModel.clearFilters()
            drawerLayout.closeDrawer(GravityCompat.END)
        }

        rvRecipes = findViewById(R.id.rvRecipes)
        adapter = RecipesAdapter(sessionManager, { recipeId ->
            RecipeDetailsActivity.start(this, recipeId)
        })
        rvRecipes.layoutManager = LinearLayoutManager(this)
        rvRecipes.adapter = adapter

        val ingredients = intent.getStringArrayListExtra(EXTRA_INGREDIENTS) ?: emptyList()
        val maxMissing = intent.getIntExtra(EXTRA_MAX_MISSING, 2)
        viewModel.searchRecipes(ingredients, maxMissing)

        viewModel.recipes.observe(this) { recipes ->
            adapter.submitList(recipes)
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
        val rgDiet = findViewById<RadioGroup>(R.id.rgDiet)

        val vege = rgDiet?.checkedRadioButtonId == R.id.rbDietVege
        val vegan = rgDiet?.checkedRadioButtonId == R.id.rbDietVegan
        val glutenFree = rgDiet?.checkedRadioButtonId == R.id.rbDietGlutenFree

        viewModel.filterRecipes(
            easy = cbEasy?.isChecked ?: false,
            medium = cbMedium?.isChecked ?: false,
            hard = cbHard?.isChecked ?: false,
            maxTime = activeDrawerMaxTime,
            maxCalories = activeDrawerMaxCalories,
            vege = vege,
            vegan = vegan,
            glutenFree = glutenFree
        )
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
        private const val EXTRA_MAX_MISSING = "extra_max_missing"

        fun start(context: Context, ingredients: List<String>, maxMissing: Int = 2) {
            val intent = Intent(context, ResultsActivity::class.java).apply {
                putStringArrayListExtra(EXTRA_INGREDIENTS, ArrayList(ingredients))
                putExtra(EXTRA_MAX_MISSING, maxMissing)
            }
            context.startActivity(intent)
        }
    }
}
