package vcmsa.projects.fkj_consultants.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.MaterialItem
import vcmsa.projects.fkj_consultants.models.MaterialListItem

class MaterialAdapter(
    private val items: List<MaterialListItem>,
    private val context: Context,
    private val addToBasketListener: (material: MaterialItem, quantity: Int, color: String?, size: String?) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_MATERIAL = 1
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategoryHeader)
    }

    inner class MaterialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.tvName)
        private val description: TextView = itemView.findViewById(R.id.tvDescription)
        private val price: TextView = itemView.findViewById(R.id.tvPrice)
        private val image: ImageView = itemView.findViewById(R.id.imgMaterial)
        private val spinnerColor: Spinner = itemView.findViewById(R.id.spinnerColor)
        private val spinnerSize: Spinner = itemView.findViewById(R.id.spinnerSize)
        private val etQuantity: EditText = itemView.findViewById(R.id.etQuantity)
        private val btnUploadLogo: Button = itemView.findViewById(R.id.btnUploadLogo)
        private val btnAddToBasket: Button = itemView.findViewById(R.id.btnAddToBasket)

        fun bind(entry: MaterialListItem.MaterialEntry) {
            val data = entry.material

            // Basic info
            name.text = data.name
            description.text = data.description
            price.text = "Price: R ${data.price}"

            // Image handling
            if (data.imageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(data.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(image)
            } else {
                image.setImageResource(R.drawable.placeholder_image)
            }

            // Color spinner
            val colorAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, data.availableColors)
            colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerColor.adapter = colorAdapter

            // Size spinner
            val sizeAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, data.availableSizes)
            sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerSize.adapter = sizeAdapter

            // Default quantity
            if (etQuantity.text.isNullOrBlank()) {
                etQuantity.setText("1")
            }

            // Upload logo (TODO: implement later)
            btnUploadLogo.setOnClickListener {
                Toast.makeText(context, "Upload logo for ${data.name}", Toast.LENGTH_SHORT).show()
            }

            // Add to basket
            btnAddToBasket.setOnClickListener {
                val quantityStr = etQuantity.text.toString()
                val quantity = quantityStr.toIntOrNull()

                if (quantity == null || quantity <= 0) {
                    Toast.makeText(context, "Please enter a valid quantity", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val selectedColor = spinnerColor.selectedItem as? String
                val selectedSize = spinnerSize.selectedItem as? String

                // ✅ Send full MaterialItem to basket
                addToBasketListener(data, quantity, selectedColor, selectedSize)

                Toast.makeText(context, "${data.name} added to basket", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is MaterialListItem.CategoryHeader -> TYPE_HEADER
            is MaterialListItem.MaterialEntry -> TYPE_MATERIAL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> {
                val view = inflater.inflate(R.layout.item_category_header, parent, false)
                HeaderViewHolder(view)
            }
            TYPE_MATERIAL -> {
                val view = inflater.inflate(R.layout.item_material, parent, false)
                MaterialViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is MaterialListItem.CategoryHeader -> (holder as HeaderViewHolder).tvCategory.text = item.categoryName
            is MaterialListItem.MaterialEntry -> (holder as MaterialViewHolder).bind(item)
        }
    }
}
