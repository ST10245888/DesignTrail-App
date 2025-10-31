package vcmsa.projects.fkj_consultants.activities

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import vcmsa.projects.fkj_consultants.R

class PromotionalAdapter(
    private val context: Context,
    private var items: List<PromotionalItem>
) : BaseAdapter(), Filterable {

    private var filteredItems: List<PromotionalItem> = items

    override fun getCount(): Int = filteredItems.size
    override fun getItem(position: Int): PromotionalItem? =
        if (position in filteredItems.indices) filteredItems[position] else null
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_promotional, parent, false)

        val item = getItem(position) ?: return view

        val imgIcon = view.findViewById<ImageView>(R.id.imgIcon)
        val txtName = view.findViewById<TextView>(R.id.txtName)
        val txtPrice = view.findViewById<TextView>(R.id.txtPrice)
        val txtStatus = view.findViewById<TextView>(R.id.txtStatus)

        txtName.text = item.name
        txtPrice.text = "R${String.format("%.2f", item.pricePerUnit)}"
        txtStatus.text = item.availability

        if (!item.imageUrl.isNullOrEmpty()) {
            Glide.with(context)
                .load(item.imageUrl)
                .placeholder(item.iconRes)
                .error(item.iconRes)
                .into(imgIcon)
        } else {
            imgIcon.setImageResource(item.iconRes)
        }

        return view
    }

    fun updateItems(newItems: List<PromotionalItem>) {
        items = newItems
        filteredItems = newItems
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val query = constraint?.toString()?.lowercase()?.trim()
            val filtered = if (query.isNullOrEmpty()) items else items.filter {
                it.name.lowercase().contains(query)
            }
            return FilterResults().apply {
                values = filtered
                count = filtered.size
            }
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredItems = (results?.values as? List<PromotionalItem>) ?: emptyList()
            notifyDataSetChanged()
        }
    }
}
