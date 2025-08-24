package vcmsa.projects.fkj_consultants.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.Product
import com.squareup.picasso.Picasso

class InventoryAdapter(
    private var items: MutableList<Product>,
    private val listener: InventoryListener
) : RecyclerView.Adapter<InventoryAdapter.VH>() {

    private val original = ArrayList(items)

    interface InventoryListener {
        fun onEdit(product: Product)
        fun onDelete(product: Product)
        fun onToggleAvailability(product: Product)
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val iv: ImageView = v.findViewById(R.id.ivItemImage)
        val tvName: TextView = v.findViewById(R.id.tvItemName)
        val tvMeta: TextView = v.findViewById(R.id.tvItemMeta)
        val tvPrice: TextView = v.findViewById(R.id.tvItemPrice)
        val btnEdit: Button = v.findViewById(R.id.btnItemEdit)
        val btnDelete: Button = v.findViewById(R.id.btnItemDelete)
        val btnToggle: Button = v.findViewById(R.id.btnItemToggle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_inventory, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]
        holder.tvName.text = p.name
        holder.tvMeta.text = "${p.category} • ${p.color} • ${p.size}"
        holder.tvPrice.text = "R %.2f".format(p.price)
        holder.btnToggle.text = if (p.availability == "In Stock") "Set Out" else "Set In"
        // load image with Picasso (ensure dependency in gradle)
        if (p.imageUrl.isNotBlank()) {
            Picasso.get().load(p.imageUrl).placeholder(R.drawable.placeholder_image).into(holder.iv)
        } else holder.iv.setImageResource(R.drawable.placeholder_image)

        holder.btnEdit.setOnClickListener { listener.onEdit(p) }
        holder.btnDelete.setOnClickListener { listener.onDelete(p) }
        holder.btnToggle.setOnClickListener { listener.onToggleAvailability(p) }
    }

    override fun getItemCount(): Int = items.size

    fun filter(query: String) {
        if (query.isBlank()) {
            items = original.toMutableList()
        } else {
            val q = query.lowercase()
            items = original.filter {
                it.name.lowercase().contains(q) ||
                        it.category.lowercase().contains(q) ||
                        it.color.lowercase().contains(q)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }
}
