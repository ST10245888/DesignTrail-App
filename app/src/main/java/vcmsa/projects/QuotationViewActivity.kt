package vcmsa.projects.fkj_consultants.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.BasketItem
import vcmsa.projects.fkj_consultants.models.Quotation

class QuotationViewActivity : AppCompatActivity() {

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

    private lateinit var quotationId: String
    private lateinit var quotation: Quotation
    private val dbRef = FirebaseDatabase.getInstance().getReference("quotations")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quotation_view)

        recyclerViewProducts = findViewById(R.id.recyclerViewProducts)
        recyclerViewProducts.layoutManager = LinearLayoutManager(this)

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

        quotationId = intent.getStringExtra("quotationId") ?: return

        loadQuotation()

        btnApprove.setOnClickListener { updateStatus("Approved") }
        btnReject.setOnClickListener { updateStatus("Rejected") }
    }

    private fun loadQuotation() {
        dbRef.child(quotationId).get().addOnSuccessListener { snapshot ->
            quotation = snapshot.getValue(Quotation::class.java) ?: return@addOnSuccessListener

            etCompanyName.setText(quotation.companyName)
            etRequesterName.setText(quotation.requesterName)
            etEmail.setText(quotation.email)
            etTel.setText(quotation.tel)
            etAddress.setText(quotation.address)
            etBillTo.setText(quotation.billTo)
            tvTotal.text = "Total: R ${quotation.total}"
            tvStatus.text = "Status: ${quotation.status}"

            recyclerViewProducts.adapter = BasketAdapter(quotation.products)
        }
    }

    private fun updateStatus(newStatus: String) {
        dbRef.child(quotationId).child("status").setValue(newStatus)
            .addOnSuccessListener {
                Toast.makeText(this, "Quotation $newStatus", Toast.LENGTH_SHORT).show()
                tvStatus.text = "Status: $newStatus"
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show()
            }
    }

    // Adapter for showing BasketItems
    class BasketAdapter(private val items: List<BasketItem>) : RecyclerView.Adapter<BasketAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvBasketItemName)
            val tvDetails: TextView = view.findViewById(R.id.tvBasketItemDetails)
            val tvPrice: TextView = view.findViewById(R.id.tvBasketItemPrice)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_basket, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            val product = item.product  // BasketItem now has a 'product' field of type Product

            holder.tvName.text = product.name
            holder.tvDetails.text = "Qty: ${item.quantity}, Color: ${product.color}, Size: ${product.size}"
            holder.tvPrice.text = "R %.2f".format(product.price * item.quantity)
        }
    }
}
