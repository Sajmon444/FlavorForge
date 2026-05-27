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

        val rgDiet = findViewById<RadioGroup>(R.id.rgDiet)

        val cbMealBreakfast = findViewById<CheckBox>(R.id.cbMealBreakfast)
        val cbMealLunch = findViewById<CheckBox>(R.id.cbMealLunch)
        val cbMealDinner = findViewById<CheckBox>(R.id.cbMealDinner)
        val cbMealDessert = findViewById<CheckBox>(R.id.cbMealDessert)
        val cbMealSnacks = findViewById<CheckBox>(R.id.cbMealSnacks)

        val sbFilterTime = findViewById<SeekBar>(R.id.sbFilterTime)
        val tvFilterTimeLabel = findViewById<TextView>(R.id.tvFilterTimeLabel)
        val sbFilterCalories = findViewById<SeekBar>(R.id.sbFilterCalories)
        val tvFilterCaloriesLabel = findViewById<TextView>(R.id.tvFilterCaloriesLabel)

        // Read intent extras and pre-populate drawer UI
        val easy = intent.getBooleanExtra(EXTRA_EASY, false)
        val medium = intent.getBooleanExtra(EXTRA_MEDIUM, false)
        val hard = intent.getBooleanExtra(EXTRA_HARD, false)
        val dietId = intent.getIntExtra(EXTRA_DIET_ID, R.id.rbDietAny)

        val breakfast = intent.getBooleanExtra(EXTRA_BREAKFAST, false)
        val lunch = intent.getBooleanExtra(EXTRA_LUNCH, false)
        val dinner = intent.getBooleanExtra(EXTRA_DINNER, false)
        val dessert = intent.getBooleanExtra(EXTRA_DESSERT, false)
        val snacks = intent.getBooleanExtra(EXTRA_SNACKS, false)

        activeDrawerMaxTime = if (intent.hasExtra(EXTRA_MAX_TIME)) intent.getIntExtra(EXTRA_MAX_TIME, 180) else null
        activeDrawerMaxCalories = if (intent.hasExtra(EXTRA_MAX_CALORIES)) intent.getIntExtra(EXTRA_MAX_CALORIES, 3000) else null

        cbEasy?.isChecked = easy
        cbMedium?.isChecked = medium
        cbHard?.isChecked = hard
        rgDiet?.check(dietId)

        cbMealBreakfast?.isChecked = breakfast
        cbMealLunch?.isChecked = lunch
        cbMealDinner?.isChecked = dinner
        cbMealDessert?.isChecked = dessert
        cbMealSnacks?.isChecked = snacks

        // Initialize SeekBars and set up listeners
        sbFilterTime?.let {
            val minutes = activeDrawerMaxTime ?: (it.progress * 15)
            it.progress = minutes / 15
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
            val kcal = activeDrawerMaxCalories ?: (300 + it.progress * 50)
            it.progress = (kcal - 300) / 50
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
            rgDiet?.check(R.id.rbDietAny)

            cbMealBreakfast?.isChecked = false
            cbMealLunch?.isChecked = false
            cbMealDinner?.isChecked = false
            cbMealDessert?.isChecked = false
            cbMealSnacks?.isChecked = false

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
        viewModel.searchRecipes(ingredients, maxMissing) {
            runOnUiThread {
                applyCurrentFilters()
            }
        }

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

        val cbMealBreakfast = findViewById<CheckBox>(R.id.cbMealBreakfast)
        val cbMealLunch = findViewById<CheckBox>(R.id.cbMealLunch)
        val cbMealDinner = findViewById<CheckBox>(R.id.cbMealDinner)
        val cbMealDessert = findViewById<CheckBox>(R.id.cbMealDessert)
        val cbMealSnacks = findViewById<CheckBox>(R.id.cbMealSnacks)

        val vege = rgDiet?.checkedRadioButtonId == R.id.rbDietVege
        val vegan = rgDiet?.checkedRadioButtonId == R.id.rbDietVegan
        val meat = rgDiet?.checkedRadioButtonId == R.id.rbDietMeat
        val fish = rgDiet?.checkedRadioButtonId == R.id.rbDietFish

        viewModel.filterRecipes(
            easy = cbEasy?.isChecked ?: false,
            medium = cbMedium?.isChecked ?: false,
            hard = cbHard?.isChecked ?: false,
            maxTime = activeDrawerMaxTime,
            maxCalories = activeDrawerMaxCalories,
            vege = vege,
            vegan = vegan,
            meat = meat,
            fish = fish,
            breakfast = cbMealBreakfast?.isChecked ?: false,
            lunch = cbMealLunch?.isChecked ?: false,
            dinner = cbMealDinner?.isChecked ?: false,
            dessert = cbMealDessert?.isChecked ?: false,
            snacks = cbMealSnacks?.isChecked ?: false
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
        private const val EXTRA_EASY = "extra_easy"
        private const val EXTRA_MEDIUM = "extra_medium"
        private const val EXTRA_HARD = "extra_hard"
        private const val EXTRA_MAX_TIME = "extra_max_time"
        private const val EXTRA_MAX_CALORIES = "extra_max_calories"
        private const val EXTRA_DIET_ID = "extra_diet_id"
        private const val EXTRA_BREAKFAST = "extra_breakfast"
        private const val EXTRA_LUNCH = "extra_lunch"
        private const val EXTRA_DINNER = "extra_dinner"
        private const val EXTRA_DESSERT = "extra_dessert"
        private const val EXTRA_SNACKS = "extra_snacks"

        fun start(
            context: Context,
            ingredients: List<String>,
            maxMissing: Int = 2,
            easy: Boolean = false,
            medium: Boolean = false,
            hard: Boolean = false,
            maxTime: Int? = null,
            maxCalories: Int? = null,
            dietId: Int = R.id.rbDietAny,
            breakfast: Boolean = false,
            lunch: Boolean = false,
            dinner: Boolean = false,
            dessert: Boolean = false,
            snacks: Boolean = false
        ) {
            val intent = Intent(context, ResultsActivity::class.java).apply {
                putStringArrayListExtra(EXTRA_INGREDIENTS, ArrayList(ingredients))
                putExtra(EXTRA_MAX_MISSING, maxMissing)
                putExtra(EXTRA_EASY, easy)
                putExtra(EXTRA_MEDIUM, medium)
                putExtra(EXTRA_HARD, hard)
                maxTime?.let { putExtra(EXTRA_MAX_TIME, it) }
                maxCalories?.let { putExtra(EXTRA_MAX_CALORIES, it) }
                putExtra(EXTRA_DIET_ID, dietId)
                putExtra(EXTRA_BREAKFAST, breakfast)
                putExtra(EXTRA_LUNCH, lunch)
                putExtra(EXTRA_DINNER, dinner)
                putExtra(EXTRA_DESSERT, dessert)
                putExtra(EXTRA_SNACKS, snacks)
            }
            context.startActivity(intent)
        }
    }
}
