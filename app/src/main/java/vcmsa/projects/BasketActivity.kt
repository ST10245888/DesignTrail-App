package vcmsa.projects.fkj_consultants.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.BasketItem

class BasketActivity : AppCompatActivity() {

    private lateinit var recyclerViewBasket: RecyclerView
    private lateinit var tvTotalPrice: TextView
    private lateinit var btnGenerateQuotation: Button

    private val basket = mutableListOf<BasketItem>()
    private lateinit var adapter: BasketAdapter
    private lateinit var databaseRef: DatabaseReference
    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basket)

        recyclerViewBasket = findViewById(R.id.recyclerViewBasket)
        tvTotalPrice = findViewById(R.id.tvTotalPrice)
        btnGenerateQuotation = findViewById(R.id.btnGenerateQuotation)

        adapter = BasketAdapter(basket)
        recyclerViewBasket.layoutManager = LinearLayoutManager(this)
        recyclerViewBasket.adapter = adapter

        databaseRef = FirebaseDatabase.getInstance().getReference("baskets").child(userId)
        loadBasketFromFirebase()

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

    private fun loadBasketFromFirebase() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                basket.clear()
                snapshot.children.forEach { child ->
                    val item = child.getValue(BasketItem::class.java)
                    if (item != null) {
                        item.firebaseKey = child.key
                        basket.add(item)
                    }
                }
                adapter.notifyDataSetChanged()
                updateTotalPrice()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@BasketActivity, "Error loading basket: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateTotalPrice() {
        val total = basket.sumOf { it.material.price * it.quantity }
        tvTotalPrice.text = "Total: R %.2f".format(total)
    }

    inner class BasketAdapter(private val items: MutableList<BasketItem>) :
        RecyclerView.Adapter<BasketAdapter.BasketViewHolder>() {

        inner class BasketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvName: TextView = itemView.findViewById(R.id.tvBasketItemName)
            val tvDetails: TextView = itemView.findViewById(R.id.tvBasketItemDetails)
            val tvPrice: TextView = itemView.findViewById(R.id.tvBasketItemPrice)
            val btnRemove: Button = itemView.findViewById(R.id.btnRemove)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BasketViewHolder {
            val view = layoutInflater.inflate(R.layout.item_basket, parent, false)
            return BasketViewHolder(view)
        }

        override fun onBindViewHolder(holder: BasketViewHolder, @SuppressLint("RecyclerView") position: Int) {
            val item = items[position]
            holder.tvName.text = item.material.name
            holder.tvDetails.text =
                "Color: ${item.selectedColor ?: "N/A"}, Size: ${item.selectedSize ?: "N/A"}, Qty: ${item.quantity}"
            holder.tvPrice.text = "Price: R %.2f".format(item.material.price * item.quantity)

            holder.btnRemove.setOnClickListener {
                val key = item.firebaseKey
                if (key.isNullOrBlank()) {
                    Toast.makeText(this@BasketActivity, "Missing key for item", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                databaseRef.child(key).removeValue().addOnSuccessListener {
                    val idx = holder.bindingAdapterPosition
                    if (idx != RecyclerView.NO_POSITION) {
                        items.removeAt(idx)
                        notifyItemRemoved(idx)
                        updateTotalPrice()
                    }
                    Toast.makeText(this@BasketActivity, "${item.material.name} removed", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this@BasketActivity, "Error removing item", Toast.LENGTH_SHORT).show()
                }
            }
        }

        override fun getItemCount(): Int = items.size
    }
}
