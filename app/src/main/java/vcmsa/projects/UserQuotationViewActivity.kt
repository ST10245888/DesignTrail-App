package vcmsa.projects.fkj_consultants.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import vcmsa.projects.fkj_consultants.R

class UserQuotationViewActivity : AppCompatActivity() {

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

    private lateinit var firestore: FirebaseFirestore
    private var quotationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quotation_view)

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

        firestore = FirebaseFirestore.getInstance()
        quotationId = intent.getStringExtra("quotationId")

        if (quotationId != null) {
            loadQuotation(quotationId!!)
        }

        btnApprove.setOnClickListener {
            updateStatus("Approved")
        }

        btnReject.setOnClickListener {
            updateStatus("Rejected")
        }
    }

    private fun loadQuotation(id: String) {
        firestore.collection("quotations").document(id).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    etCompanyName.setText(doc.getString("companyName") ?: "")
                    etRequesterName.setText(doc.getString("requesterName") ?: "")
                    etEmail.setText(doc.getString("email") ?: "")
                    etTel.setText(doc.getString("phone") ?: "")
                    etAddress.setText(doc.getString("address") ?: "")
                    etBillTo.setText(doc.getString("billTo") ?: "")
                    tvTotal.text = "Total: R ${doc.getDouble("subtotal") ?: 0.0}"
                    tvStatus.text = "Status: ${doc.getString("status") ?: "Pending"}"
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load quotation", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateStatus(status: String) {
        quotationId?.let { id ->
            firestore.collection("quotations").document(id)
                .update("status", status)
                .addOnSuccessListener {
                    Toast.makeText(this, "Quotation $status", Toast.LENGTH_SHORT).show()
                    tvStatus.text = "Status: $status"
                    finish() // Go back to list
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
