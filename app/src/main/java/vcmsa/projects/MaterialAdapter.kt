package vcmsa.projects.fkj_consultants.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.MaterialListItem

class MaterialAdapter(
    private val items: List<MaterialListItem>,
    private val context: Context,
    private val addToBasketListener: (materialId: String, quantity: Int, color: String?, size: String?) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_MATERIAL = 1
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategoryHeader)
    }

    inner class MaterialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvName)
        val description: TextView = itemView.findViewById(R.id.tvDescription)
        val price: TextView = itemView.findViewById(R.id.tvPrice)
        val image: ImageView = itemView.findViewById(R.id.imgMaterial)
        val spinnerColor: Spinner = itemView.findViewById(R.id.spinnerColor)
        val spinnerSize: Spinner = itemView.findViewById(R.id.spinnerSize)
        val etQuantity: EditText = itemView.findViewById(R.id.etQuantity)
        val btnUploadLogo: Button = itemView.findViewById(R.id.btnUploadLogo)
        val btnAddToBasket: Button = itemView.findViewById(R.id.btnAddToBasket)

        fun bind(material: MaterialListItem.MaterialEntry) {
            val data = material.material

            name.text = data.name
            description.text = data.description
            price.text = "Price: R ${data.price}"

            // Load image safely
            if (!data.imageUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(data.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(image)
            } else {
                image.setImageResource(R.drawable.placeholder_image)
            }

            // Setup Color Spinner
            val colorAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, data.availableColors)
            colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerColor.adapter = colorAdapter

            // Setup Size Spinner
            val sizeAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, data.availableSizes)
            sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerSize.adapter = sizeAdapter

            // Set default quantity to 1 if empty
            if (etQuantity.text.isNullOrBlank()) {
                etQuantity.setText("1")
            }

            // Upload logo click
            btnUploadLogo.setOnClickListener {
                Toast.makeText(context, "Upload logo for ${data.name}", Toast.LENGTH_SHORT).show()
                // TODO: Launch logo picker dialog or activity
            }

            // Add to basket click
            btnAddToBasket.setOnClickListener {
                val quantityStr = etQuantity.text.toString()
                val quantity = quantityStr.toIntOrNull()
                if (quantity == null || quantity <= 0) {
                    Toast.makeText(context, "Please enter a valid quantity", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val selectedColor = spinnerColor.selectedItem as? String
                val selectedSize = spinnerSize.selectedItem as? String

                addToBasketListener(data.id, quantity, selectedColor, selectedSize)
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
            is MaterialListItem.CategoryHeader -> {
                (holder as HeaderViewHolder).tvCategory.text = item.categoryName
            }
            is MaterialListItem.MaterialEntry -> {
                (holder as MaterialViewHolder).bind(item)
            }
        }
    }
}
