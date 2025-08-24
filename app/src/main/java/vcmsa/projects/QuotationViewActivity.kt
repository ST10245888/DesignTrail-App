package vcmsa.projects.fkj_consultants.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.adapters.MaterialAdapter

import vcmsa.projects.fkj_consultants.models.Quotation
import vcmsa.projects.fkj_consultants.models.MaterialListItem

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

            // Convert BasketItems to MaterialListItems for display
            val materialListItems = quotation.products.flatMap { basketItem ->
                listOf(MaterialListItem.MaterialEntry(basketItem.material))
            }

            recyclerViewProducts.adapter = MaterialAdapter(
                materialListItems,
                this
            ) { _, _, _, _ ->
                // Read-only view, no add to basket
            }
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
}
