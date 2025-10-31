package vcmsa.projects.fkj_consultants.activities

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.Quotation
import vcmsa.projects.fkj_consultants.models.QuotationItem
import java.io.File

class AdminQuotationViewActivity : AppCompatActivity() {

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

    private lateinit var quotationFile: File
    private lateinit var quotation: Quotation
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_quotation_view)

        // Initialize views
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

        val filePath = intent.getStringExtra("quotationFilePath")
        if (filePath == null) {
            Toast.makeText(this, "Quotation file not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        quotationFile = File(filePath)

        // Initialize Firebase reference
        dbRef = FirebaseDatabase.getInstance().getReference("quotations")

        loadQuotation()

        btnApprove.setOnClickListener { updateStatus("Approved") }
        btnReject.setOnClickListener { updateStatus("Rejected") }

        highlightFirstReviewStatus()
    }

    private fun loadQuotation() {
        val lines = quotationFile.readLines()
        var company = ""
        var requester = ""
        var email = ""
        var phone = ""
        var address = ""
        var billTo = ""
        var status = "Pending"
        val items = mutableListOf<QuotationItem>()

        for (line in lines) {
            when {
                line.startsWith("Company:") -> company = line.substringAfter("Company:").trim()
                line.startsWith("Requester:") -> requester = line.substringAfter("Requester:").trim()
                line.startsWith("Email:") -> email = line.substringAfter("Email:").trim()
                line.startsWith("Phone:") -> phone = line.substringAfter("Phone:").trim()
                line.startsWith("Address:") -> address = line.substringAfter("Address:").trim()
                line.startsWith("BillTo:") -> billTo = line.substringAfter("BillTo:").trim()
                line.startsWith("Status:") -> status = line.substringAfter("Status:").trim()
                line.startsWith("Item:") -> {
                    val parts = line.substringAfter("Item:").split(",")
                    if (parts.size >= 4) {
                        items.add(
                            QuotationItem(
                                name = parts[0].trim(),
                                pricePerUnit = parts[1].trim().toDoubleOrNull() ?: 0.0,
                                quantity = parts[2].trim().toIntOrNull() ?: 0,
                                productId = parts[3].trim()
                            )
                        )
                    }
                }
            }
        }

        quotation = Quotation(
            id = extractQuotationIdFromFileName(quotationFile.name) ?: "",
            userId = "", // set if known
            companyName = company,
            address = address,
            email = email,
            phone = phone,
            billTo = billTo,
            status = status,
            fileName = quotationFile.name,
            filePath = quotationFile.absolutePath,
            timestamp = quotationFile.lastModified(),
            items = items
        )

        // Populate views
        etCompanyName.setText(company)
        etRequesterName.setText(requester)
        etEmail.setText(email)
        etTel.setText(phone)
        etAddress.setText(address)
        etBillTo.setText(billTo)
        tvTotal.text = "Total: R %.2f".format(quotation.subtotal)
        updateStatusView(status)
    }

    private fun updateStatus(newStatus: String) {
        // Update local file
        if (quotationFile.exists()) {
            val lines = quotationFile.readLines().toMutableList()
            var statusUpdated = false
            for (i in lines.indices) {
                if (lines[i].startsWith("Status:")) {
                    lines[i] = "Status: $newStatus"
                    statusUpdated = true
                    break
                }
            }
            if (!statusUpdated) lines.add("Status: $newStatus")
            quotationFile.writeText(lines.joinToString("\n"))
        }

        // Update in-memory object and UI
        quotation.status = newStatus
        updateStatusView(newStatus)

        // Update Firebase
        if (quotation.id.isNotEmpty()) {
            dbRef.child(quotation.id).child("status").setValue(newStatus)
                .addOnSuccessListener {
                    Toast.makeText(this, "Quotation $newStatus", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update Firebase: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateStatusView(status: String) {
        tvStatus.text = "Status: $status"
        tvStatus.setTextColor(
            when (status.lowercase()) {
                "pending" -> Color.GRAY
                "approved" -> Color.parseColor("#4CAF50")
                "rejected" -> Color.parseColor("#F44336")
                else -> Color.BLACK
            }
        )
    }

    private fun highlightFirstReviewStatus() {
        if (quotation.status.lowercase() == "pending") {
            tvStatus.text = "Status: Pending (First Review)"
            tvStatus.setTextColor(Color.parseColor("#FF9800"))
        }
    }

    private fun extractQuotationIdFromFileName(fileName: String): String? {
        return fileName.substringBefore("_")
    }
}
