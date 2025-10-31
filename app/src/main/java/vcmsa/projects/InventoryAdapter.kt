package vcmsa.projects.fkj_consultants.activities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.Product
import java.text.NumberFormat
import java.util.*

class InventoryAdapter(
    private val listener: InventoryListener
) : ListAdapter<Product, InventoryAdapter.InventoryViewHolder>(DiffCallback()) {

    interface InventoryListener {
        fun onEdit(product: Product)
        fun onDelete(product: Product)
        fun onToggleAvailability(product: Product)
    }

    inner class InventoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        val tvName: TextView = itemView.findViewById(R.id.tvItemName)
        val tvDescription: TextView = itemView.findViewById(R.id.tvItemDescription)
        val tvPrice: TextView = itemView.findViewById(R.id.tvItemPrice)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvItemQuantity)
        val tvCategory: TextView = itemView.findViewById(R.id.tvItemCategory)
        val btnAvailability: Button = itemView.findViewById(R.id.btnAvailability)
        val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        init {
            btnEdit.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onEdit(getItem(position))
                }
            }

            btnDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDelete(getItem(position))
                }
            }

            btnAvailability.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onToggleAvailability(getItem(position))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inventory, parent, false)
        return InventoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        val product = getItem(position)

        holder.tvName.text = product.name
        holder.tvDescription.text = product.description.ifEmpty { "No description" }

        // Format price with currency
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        holder.tvPrice.text = "Price: ${currencyFormat.format(product.price)}"

        holder.tvQuantity.text = "Qty: ${product.quantity}"
        holder.tvCategory.text = if (product.category.isNotEmpty()) product.category else "Uncategorized"

        // Load product image
        if (product.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.ic_add)
                .error(R.drawable.ic_add)
                .centerCrop()
                .into(holder.ivProductImage)
        } else {
            holder.ivProductImage.setImageResource(R.drawable.ic_add)
        }

        // Set availability button
        holder.btnAvailability.text = product.availability
        val color = if (product.availability == "In Stock")
            R.color.status_online
        else
            R.color.status_offline
        holder.btnAvailability.setBackgroundColor(holder.itemView.context.getColor(color))
    }

    class DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean =
            oldItem.productId == newItem.productId

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean =
            oldItem == newItem
    }
}
