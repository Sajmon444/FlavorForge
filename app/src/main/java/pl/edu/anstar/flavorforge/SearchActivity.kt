package pl.edu.anstar.flavorforge

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.GravityCompat
import dagger.hilt.android.AndroidEntryPoint
import pl.edu.anstar.flavorforge.ui.search.SearchViewModel
import kotlin.text.trim

@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {

    private val viewModel: SearchViewModel by viewModels()
    private lateinit var etIngredient: AutoCompleteTextView
    private lateinit var btnAdd: Button
    private lateinit var btnSearch: Button
    private lateinit var rvIngredients: RecyclerView
    private lateinit var adapter: IngredientsAdapter
    private lateinit var drawerLayout: DrawerLayout

    private var activeDrawerMaxTime: Int? = null
    private var activeDrawerMaxCalories: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        drawerLayout = findViewById(R.id.drawerLayout)
        etIngredient = findViewById(R.id.etIngredient)
        btnAdd = findViewById(R.id.btnAdd)
        btnSearch = findViewById(R.id.btnSearch)
        rvIngredients = findViewById(R.id.rvIngredients)


        val suggestions = resources.getStringArray(R.array.ingredients_suggestions).toList()

        val autoAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            suggestions
        )

        etIngredient.setAdapter(autoAdapter)
        etIngredient.threshold = 1


        etIngredient.setAdapter(autoAdapter)
        etIngredient.threshold = 1
        adapter = IngredientsAdapter { ingredient ->
            viewModel.removeIngredient(ingredient)
        }

        rvIngredients.layoutManager = LinearLayoutManager(this)
        rvIngredients.adapter = adapter

        viewModel.ingredients.observe(this) { ingredients ->
            adapter.submitList(ingredients)
            btnSearch.isEnabled = ingredients.isNotEmpty()
        }

        btnAdd.setOnClickListener {
            val text = etIngredient.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.addIngredient(text)
                etIngredient.setText("")
            }
        }

        val sbMaxMissing = findViewById<SeekBar>(R.id.sbMaxMissing)
        val tvMaxMissingLabel = findViewById<TextView>(R.id.tvMaxMissingLabel)

        // Dynamically set label according to device language
        tvMaxMissingLabel.text = getString(R.string.max_missing_ingredients, sbMaxMissing.progress)

        sbMaxMissing.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvMaxMissingLabel.text = getString(R.string.max_missing_ingredients, progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

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

        findViewById<View>(R.id.btnApplyFilters)?.setOnClickListener {
            activeDrawerMaxTime = sbFilterTime?.progress?.let { it * 15 }
            activeDrawerMaxCalories = sbFilterCalories?.progress?.let { 300 + it * 50 }
            drawerLayout.closeDrawer(GravityCompat.END)
        }

        findViewById<View>(R.id.btnClearFilters)?.setOnClickListener {
            findViewById<CheckBox>(R.id.cbEasy)?.isChecked = false
            findViewById<CheckBox>(R.id.cbMedium)?.isChecked = false
            findViewById<CheckBox>(R.id.cbHard)?.isChecked = false
            findViewById<RadioGroup>(R.id.rgDiet)?.check(R.id.rbDietAny)

            findViewById<CheckBox>(R.id.cbMealBreakfast)?.isChecked = false
            findViewById<CheckBox>(R.id.cbMealLunch)?.isChecked = false
            findViewById<CheckBox>(R.id.cbMealDinner)?.isChecked = false
            findViewById<CheckBox>(R.id.cbMealDessert)?.isChecked = false
            findViewById<CheckBox>(R.id.cbMealSnacks)?.isChecked = false

            sbFilterTime?.progress = 12
            sbFilterCalories?.progress = 54
            activeDrawerMaxTime = null
            activeDrawerMaxCalories = null

            drawerLayout.closeDrawer(GravityCompat.END)
        }

        btnSearch.setOnClickListener {
            val ingredients = viewModel.ingredients.value ?: return@setOnClickListener
            val maxMissing = sbMaxMissing.progress

            val cbEasy = findViewById<CheckBox>(R.id.cbEasy)
            val cbMedium = findViewById<CheckBox>(R.id.cbMedium)
            val cbHard = findViewById<CheckBox>(R.id.cbHard)
            val rgDiet = findViewById<RadioGroup>(R.id.rgDiet)

            val cbMealBreakfast = findViewById<CheckBox>(R.id.cbMealBreakfast)
            val cbMealLunch = findViewById<CheckBox>(R.id.cbMealLunch)
            val cbMealDinner = findViewById<CheckBox>(R.id.cbMealDinner)
            val cbMealDessert = findViewById<CheckBox>(R.id.cbMealDessert)
            val cbMealSnacks = findViewById<CheckBox>(R.id.cbMealSnacks)

            ResultsActivity.start(
                context = this,
                ingredients = ingredients,
                maxMissing = maxMissing,
                easy = cbEasy?.isChecked ?: false,
                medium = cbMedium?.isChecked ?: false,
                hard = cbHard?.isChecked ?: false,
                maxTime = activeDrawerMaxTime,
                maxCalories = activeDrawerMaxCalories,
                dietId = rgDiet?.checkedRadioButtonId ?: R.id.rbDietAny,
                breakfast = cbMealBreakfast?.isChecked ?: false,
                lunch = cbMealLunch?.isChecked ?: false,
                dinner = cbMealDinner?.isChecked ?: false,
                dessert = cbMealDessert?.isChecked ?: false,
                snacks = cbMealSnacks?.isChecked ?: false
            )
        }

        findViewById<View>(R.id.btnFilters).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        findViewById<View>(R.id.btnMenu).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        findViewById<Button>(R.id.btnHome).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<Button>(R.id.btnRecipes).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, RecipesActivity::class.java))
        }

        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, SignInActivity::class.java))
        }

        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}

class IngredientsAdapter(
    private val onRemove: (String) -> Unit
) : RecyclerView.Adapter<IngredientsAdapter.ViewHolder>() {

    private var items: List<String> = emptyList()

    fun submitList(newItems: List<String>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredient, parent, false)
        return ViewHolder(view, onRemove)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class ViewHolder(
        itemView: View,
        private val onRemove: (String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvIngredient: TextView = itemView.findViewById(R.id.tvIngredient)
        private val btnRemove: Button = itemView.findViewById(R.id.btnRemove)

        fun bind(ingredient: String) {
            tvIngredient.text = "• $ingredient"
            btnRemove.setOnClickListener { onRemove(ingredient) }
        }
    }
}
