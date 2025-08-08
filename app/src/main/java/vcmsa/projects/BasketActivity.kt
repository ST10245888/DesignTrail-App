package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.parcelize.Parcelize
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.MaterialItem

class BasketActivity : AppCompatActivity() {

    private lateinit var recyclerViewBasket: RecyclerView
    private lateinit var tvTotalPrice: TextView
    private lateinit var btnGenerateQuotation: Button

    // Basket list
    private val basket = mutableListOf<BasketItem>()

    @Parcelize
    data class BasketItem(
        val material: MaterialItem,
        val quantity: Int,
        val selectedColor: String?,
        val selectedSize: String?
    ) : Parcelable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basket)

        recyclerViewBasket = findViewById(R.id.recyclerViewBasket)
        tvTotalPrice = findViewById(R.id.tvTotalPrice)
        btnGenerateQuotation = findViewById(R.id.btnGenerateQuotation)

        // TODO: Load basket from intent or persistent storage instead of hardcoded
        basket.add(
            BasketItem(
                material = MaterialItem(
                    id = "1",
                    name = "Sample Material",
                    description = "Sample Desc",
                    imageUrl = "",
                    price = 100.0,
                    availableColors = listOf("Red"),
                    availableSizes = listOf("M"),
                    category = "Category1"
                ),
                quantity = 2,
                selectedColor = "Red",
                selectedSize = "M"
            )
        )

        recyclerViewBasket.layoutManager = LinearLayoutManager(this)
        recyclerViewBasket.adapter = BasketAdapter(basket)

        updateTotalPrice()

        btnGenerateQuotation.setOnClickListener {
            if (basket.isEmpty()) {
                Toast.makeText(this, "Your basket is empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, QuotationGeneratorActivity::class.java)
            intent.putParcelableArrayListExtra("basket_items", ArrayList(basket))
            startActivity(intent)
        }
    }

    private fun updateTotalPrice() {
        val total = basket.sumOf { it.material.price * it.quantity }
        tvTotalPrice.text = "Total: R %.2f".format(total)
    }

    inner class BasketAdapter(private val items: MutableList<BasketItem>) :
        RecyclerView.Adapter<BasketAdapter.BasketViewHolder>() {

        inner class BasketViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
            val tvName: TextView = itemView.findViewById(R.id.tvBasketItemName)
            val tvDetails: TextView = itemView.findViewById(R.id.tvBasketItemDetails)
            val tvPrice: TextView = itemView.findViewById(R.id.tvBasketItemPrice)
            val btnRemove: Button = itemView.findViewById(R.id.btnRemove)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): BasketViewHolder {
            val view = layoutInflater.inflate(R.layout.item_basket, parent, false)
            return BasketViewHolder(view)
        }

        override fun onBindViewHolder(holder: BasketViewHolder, position: Int) {
            val item = items[position]
            holder.tvName.text = item.material.name
            holder.tvDetails.text =
                "Color: ${item.selectedColor ?: "N/A"}, Size: ${item.selectedSize ?: "N/A"}, Qty: ${item.quantity}"
            holder.tvPrice.text = "Price: R %.2f".format(item.material.price * item.quantity)

            holder.btnRemove.setOnClickListener {
                items.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, items.size)
                updateTotalPrice()
                Toast.makeText(this@BasketActivity, "${item.material.name} removed from basket", Toast.LENGTH_SHORT).show()
            }
        }

        override fun getItemCount(): Int = items.size
    }
}
