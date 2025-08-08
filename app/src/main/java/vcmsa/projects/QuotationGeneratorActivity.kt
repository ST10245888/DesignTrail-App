package vcmsa.projects.fkj_consultants.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.MaterialItem

class QuotationGeneratorActivity : AppCompatActivity() {

    private lateinit var recyclerViewProducts: RecyclerView
    private lateinit var etCompanyName: EditText
    private lateinit var etRequesterName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etTel: EditText
    private lateinit var etAddress: EditText
    private lateinit var etBillTo: EditText
    private lateinit var tvTotal: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnApprove: Button
    private lateinit var btnReject: Button

    private var basketItems: List<BasketActivity.BasketItem> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quotation_generator)

        recyclerViewProducts = findViewById(R.id.recyclerViewProducts)
        etCompanyName = findViewById(R.id.etCompanyName)
        etRequesterName = findViewById(R.id.etRequesterName)
        etEmail = findViewById(R.id.etEmail)
        etTel = findViewById(R.id.etTel)
        etAddress = findViewById(R.id.etAddress)
        etBillTo = findViewById(R.id.etBillTo)
        tvTotal = findViewById(R.id.tvTotal)
        tvStatus = findViewById(R.id.tvStatus)
        btnApprove = findViewById(R.id.btnApprove)
        btnReject = findViewById(R.id.btnReject)

        basketItems = intent.getParcelableArrayListExtra("basket_items") ?: listOf()

        recyclerViewProducts.layoutManager = LinearLayoutManager(this)
        recyclerViewProducts.adapter = QuotationAdapter(basketItems)

        updateTotalPrice()

        btnApprove.setOnClickListener {
            tvStatus.text = "Approved"
            Toast.makeText(this, "Quotation Approved", Toast.LENGTH_SHORT).show()
        }

        btnReject.setOnClickListener {
            tvStatus.text = "Rejected"
            Toast.makeText(this, "Quotation Rejected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTotalPrice() {
        val total = basketItems.sumOf { it.material.price * it.quantity }
        tvTotal.text = "R %.2f".format(total)
    }

    class QuotationAdapter(private val items: List<BasketActivity.BasketItem>) :
        RecyclerView.Adapter<QuotationAdapter.ViewHolder>() {

        inner class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvProductName)
            val tvDesc: TextView = view.findViewById(R.id.tvDescription)
            val tvQty: TextView = view.findViewById(R.id.tvQuantity)
            val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_quotation_product, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tvName.text = item.material.name
            holder.tvDesc.text = item.material.description
            holder.tvQty.text = "Qty: ${item.quantity}, Color: ${item.selectedColor}"
            holder.tvPrice.text = "R %.2f".format(item.material.price * item.quantity)
        }

        override fun getItemCount(): Int = items.size
    }
}
