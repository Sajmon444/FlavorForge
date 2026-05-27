package pl.edu.anstar.flavorforge

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
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

        btnSearch.setOnClickListener {
            val ingredients = viewModel.ingredients.value ?: return@setOnClickListener
            ResultsActivity.start(this, ingredients)
        }

        findViewById<View>(R.id.btnFilters).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        findViewById<View>(R.id.btnApplyFilters).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
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
