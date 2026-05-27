package pl.edu.anstar.flavorforge

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.SeekBar
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

    private var activeDrawerMaxTime: Int? = null
    private var activeDrawerMaxCalories: Int? = null

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

        findViewById<Button>(R.id.btnApplyFilters)?.setOnClickListener {
            activeDrawerMaxTime = sbFilterTime?.progress?.let { it * 15 }
            activeDrawerMaxCalories = sbFilterCalories?.progress?.let { 300 + it * 50 }
            applyCurrentFilters()
            drawerLayout.closeDrawer(GravityCompat.END)
        }

        findViewById<Button>(R.id.btnClearFilters)?.setOnClickListener {
            findViewById<CheckBox>(R.id.cbEasy)?.isChecked = false
            findViewById<CheckBox>(R.id.cbMedium)?.isChecked = false
            findViewById<CheckBox>(R.id.cbHard)?.isChecked = false
            findViewById<RadioGroup>(R.id.rgDiet)?.check(R.id.rbDietAny)

            sbFilterTime?.progress = 12
            sbFilterCalories?.progress = 54
            activeDrawerMaxTime = null
            activeDrawerMaxCalories = null

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
}
