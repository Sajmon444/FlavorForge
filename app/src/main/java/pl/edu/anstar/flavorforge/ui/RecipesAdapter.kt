package pl.edu.anstar.flavorforge.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pl.edu.anstar.flavorforge.R
import pl.edu.anstar.flavorforge.data.model.RecipeSearchResult

class RecipesAdapter(
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<RecipesAdapter.ViewHolder>() {

    private var items: List<RecipeSearchResult> = emptyList()

    fun submitList(newItems: List<RecipeSearchResult>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class ViewHolder(
        itemView: View,
        private val onItemClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val ivRecipe: ImageView = itemView.findViewById(R.id.ivRecipe)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvRecipeTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvRecipeDescription)

        fun bind(recipe: RecipeSearchResult) {
            tvTitle.text = recipe.title
            tvDescription.text = recipe.description

            itemView.setOnClickListener { onItemClick(recipe.id) }

            Glide.with(itemView.context)
                .load(recipe.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(ivRecipe)
        }
    }
}
