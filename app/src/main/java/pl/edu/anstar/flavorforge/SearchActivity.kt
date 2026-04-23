package pl.edu.anstar.flavorforge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import pl.edu.anstar.flavorforge.ui.search.SearchViewModel

@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {

    private val viewModel: SearchViewModel by viewModels()
    private lateinit var etIngredient: EditText
    private lateinit var btnAdd: Button
    private lateinit var btnSearch: Button
    private lateinit var rvIngredients: RecyclerView
    private lateinit var adapter: IngredientsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        etIngredient = findViewById(R.id.etIngredient)
        btnAdd = findViewById(R.id.btnAdd)
        btnSearch = findViewById(R.id.btnSearch)
        rvIngredients = findViewById(R.id.rvIngredients)

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