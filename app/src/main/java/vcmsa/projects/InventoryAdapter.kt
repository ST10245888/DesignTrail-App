package vcmsa.projects.fkj_consultants.activities

import vcmsa.projects.fkj_consultants.models.Product
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.fkj_consultants.R

class InventoryAdapter(
    private var items: MutableList<Product>,
    private val listener: InventoryListener
) : RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>() {

    private var filteredItems: MutableList<Product> = items.toMutableList()

    interface InventoryListener {
        fun onEdit(product: Product)
        fun onDelete(product: Product)
        fun onToggleAvailability(product: Product)
    }

    inner class InventoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvItemName)
        val tvDescription: TextView = itemView.findViewById(R.id.tvItemDescription)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvItemQuantity)

        init {
            itemView.setOnClickListener {
                listener.onEdit(filteredItems[adapterPosition])
            }
            itemView.setOnLongClickListener {
                listener.onDelete(filteredItems[adapterPosition])
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inventory, parent, false)
        return InventoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        val product = filteredItems[position]
        holder.tvName.text = product.name
        holder.tvDescription.text = product.description
        holder.tvQuantity.text = "Qty: ${product.quantity}"
    }

    override fun getItemCount(): Int = filteredItems.size

    fun filter(query: String) {
        filteredItems = if (query.isEmpty()) {
            items.toMutableList()
        } else {
            items.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }

    fun updateData(newItems: List<Product>) {
        items = newItems.toMutableList()
        filteredItems = items.toMutableList()
        notifyDataSetChanged()
    }
}
