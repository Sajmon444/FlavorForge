package pl.edu.anstar.flavorforge.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pl.edu.anstar.flavorforge.R
import pl.edu.anstar.flavorforge.data.local.SessionManager
import pl.edu.anstar.flavorforge.data.model.RecipeSearchResult

class RecipesAdapter(
    private val sessionManager: SessionManager,
    private val onItemClick: (Int) -> Unit,
    private val onFavoriteClick: ((RecipeSearchResult) -> Unit)? = null
) : RecyclerView.Adapter<RecipesAdapter.ViewHolder>() {

    private var items: List<RecipeSearchResult> = emptyList()

    fun submitList(newItems: List<RecipeSearchResult>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return ViewHolder(view, sessionManager, onItemClick, onFavoriteClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class ViewHolder(
        itemView: View,
        private val sessionManager: SessionManager,
        private val onItemClick: (Int) -> Unit,
        private val onFavoriteClick: ((RecipeSearchResult) -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {
        private val ivRecipe: ImageView = itemView.findViewById(R.id.ivRecipe)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvRecipeTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvRecipeDescription)
        private val tvMetadata: TextView = itemView.findViewById(R.id.tvRecipeMetadata)
        private val tvDifficulty: TextView = itemView.findViewById(R.id.tvRecipeDifficulty)
        private val btnFavorite: ImageView = itemView.findViewById(R.id.btnFavorite)

        fun bind(recipe: RecipeSearchResult) {
            val language = sessionManager.getSettingsPrefs().getString("app_lang", "pl") ?: "pl"
            tvTitle.text = recipe.getTitle(language)
            tvDescription.text = recipe.getDescription(language)

            val metadata = mutableListOf<String>()
            recipe.caloriesTotal?.let { if (it > 0) metadata.add("$it kcal") }
            recipe.prepTime?.let { if (it > 0) metadata.add("$it min") }

            if (metadata.isNotEmpty()) {
                tvMetadata.visibility = View.VISIBLE
                tvMetadata.text = metadata.joinToString(" • ")
            } else {
                tvMetadata.visibility = View.GONE
            }

            val context = itemView.context
            val diff = recipe.difficulty?.lowercase() ?: ""
            if (diff.isNotEmpty()) {
                tvDifficulty.visibility = View.VISIBLE
                tvDifficulty.text = when {
                    diff.contains("easy") || diff.contains("łatw") || diff == "1" -> 
                        "★ " + context.getString(R.string.difficulty_easy)
                    diff.contains("medium") || diff.contains("średn") || diff == "2" -> 
                        "★★ " + context.getString(R.string.difficulty_medium)
                    diff.contains("hard") || diff.contains("trudn") || diff == "3" -> 
                        "★★★ " + context.getString(R.string.difficulty_hard)
                    else -> recipe.difficulty
                }
            } else {
                tvDifficulty.visibility = View.GONE
            }

            // Favorites heart status and click listener
            val isFavorite = sessionManager.isFavorite(recipe.id)
            if (isFavorite) {
                btnFavorite.setColorFilter(Color.parseColor("#FF5252")) // bright red
            } else {
                btnFavorite.setColorFilter(Color.parseColor("#E8EBEE")) // light grey
            }

            btnFavorite.setOnClickListener {
                if (!sessionManager.isLoggedIn()) {
                    Toast.makeText(context, "Zaloguj się, aby zapisywać ulubione przepisy!", Toast.LENGTH_SHORT).show()
                } else {
                    val currentlyFavorite = sessionManager.isFavorite(recipe.id)
                    if (currentlyFavorite) {
                        sessionManager.removeFavorite(recipe.id)
                        btnFavorite.setColorFilter(Color.parseColor("#E8EBEE"))
                        Toast.makeText(context, "Usunięto z ulubionych", Toast.LENGTH_SHORT).show()
                    } else {
                        sessionManager.addFavorite(recipe.id)
                        btnFavorite.setColorFilter(Color.parseColor("#FF5252"))
                        Toast.makeText(context, "Dodano do ulubionych", Toast.LENGTH_SHORT).show()
                    }
                    onFavoriteClick?.invoke(recipe)
                }
            }

            itemView.setOnClickListener { onItemClick(recipe.id) }

            Glide.with(itemView.context)
                .load(recipe.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(ivRecipe)
        }
    }
}
