package pl.edu.anstar.flavorforge

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import pl.edu.anstar.flavorforge.data.model.RecipeSearchResult
import pl.edu.anstar.flavorforge.ui.results.ResultsViewModel

@AndroidEntryPoint
class ResultsActivity : AppCompatActivity() {

    private val viewModel: ResultsViewModel by viewModels()
    private lateinit var rvRecipes: RecyclerView
    private lateinit var adapter: RecipesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        findViewById<android.widget.ImageView>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        rvRecipes = findViewById(R.id.rvRecipes)
        adapter = RecipesAdapter()
        rvRecipes.layoutManager = LinearLayoutManager(this)
        rvRecipes.adapter = adapter

        val ingredients = intent.getStringArrayListExtra(EXTRA_INGREDIENTS) ?: emptyList()
        viewModel.searchRecipes(ingredients)

        viewModel.recipes.observe(this) { recipes ->
            adapter.submitList(recipes)
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

class RecipesAdapter : RecyclerView.Adapter<RecipesAdapter.ViewHolder>() {

    private var items: List<RecipeSearchResult> = emptyList()

    fun submitList(newItems: List<RecipeSearchResult>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivRecipe: ImageView = itemView.findViewById(R.id.ivRecipe)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvRecipeTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvRecipeDescription)

        fun bind(recipe: RecipeSearchResult) {
            tvTitle.text = recipe.title
            tvDescription.text = recipe.description

            Glide.with(itemView.context)
                .load(recipe.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(ivRecipe)
        }
    }
}