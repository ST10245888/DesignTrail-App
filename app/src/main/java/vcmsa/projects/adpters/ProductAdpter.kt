package vcmsa.projects.adpters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.Product

class ProductAdapter(
    private val productList: MutableList<Product>,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    // (Google, 2024)
    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvProductName)
        val desc: TextView = itemView.findViewById(R.id.tvProductDesc)
        val price: TextView = itemView.findViewById(R.id.tvProductPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        //(Google Developers, 2024)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun getItemCount(): Int = productList.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        //(JetBrains, 2023)
        val product = productList[position]
        holder.name.text = product.name
        holder.desc.text = product.description
        holder.price.text = "R ${product.price}"

        holder.itemView.setOnClickListener { onItemClick(product) }
    }
}

/**
 * References
 *
 * Google .2024. RecyclerView Overview. Android Developers. Available at: https://developer.android.com/guide/topics/ui/layout/recyclerview (Accessed: 4 November 2025).
 *
 * Google Developers .2024. LayoutInflater documentation. Available at: https://developer.android.com/reference/android/view/LayoutInflater (Accessed: 4 November 2025).
 *
 * JetBrains .2023. Kotlin Android development documentation. Available at: https://kotlinlang.org/docs/android-overview.html (Accessed: 4 November 2025).
 */
