package vcmsa.projects.fkj_consultants.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import vcmsa.projects.fkj_consultants.R
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_quotation_view)

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
        loadQuotation()

        btnApprove.setOnClickListener { updateStatus("Approved") }
        btnReject.setOnClickListener { updateStatus("Rejected") }
    }

    private fun loadQuotation() {
        val lines = quotationFile.readLines()
        var company = ""
        var requester = ""
        var email = ""
        var phone = ""
        var address = ""
        var billTo = ""
        var subtotal = 0.0
        var status = "Pending"

        for (line in lines) {
            when {
                line.startsWith("Company:") -> company = line.substringAfter("Company:").trim()
                line.startsWith("Requester:") -> requester = line.substringAfter("Requester:").trim()
                line.startsWith("Email:") -> email = line.substringAfter("Email:").trim()
                line.startsWith("Phone:") -> phone = line.substringAfter("Phone:").trim()
                line.startsWith("Address:") -> address = line.substringAfter("Address:").trim()
                line.startsWith("BillTo:") -> billTo = line.substringAfter("BillTo:").trim()
                line.startsWith("Subtotal:") -> subtotal = line.substringAfter("Subtotal:").trim().toDoubleOrNull() ?: 0.0
                line.startsWith("Status:") -> status = line.substringAfter("Status:").trim()
            }
        }

        quotation = Quotation(
            companyName = company,
            address = address,
            email = email,
            phone = phone,
            billTo = billTo,
            subtotal = subtotal,
            status = status,
            filePath = quotationFile.absolutePath
        )

        // Populate views
        etCompanyName.setText(company)
        etRequesterName.setText(requester)
        etEmail.setText(email)
        etTel.setText(phone)
        etAddress.setText(address)
        etBillTo.setText(billTo)
        tvTotal.text = "Total: R %.2f".format(subtotal)
        tvStatus.text = "Status: $status"
    }

    private fun updateStatus(newStatus: String) {
        if (!quotationFile.exists()) return

        val lines = quotationFile.readLines().toMutableList()
        var statusUpdated = false
        for (i in lines.indices) {
            if (lines[i].startsWith("Status:")) {
                lines[i] = "Status: $newStatus"
                statusUpdated = true
                break
            }
        }
        if (!statusUpdated) {
            lines.add("Status: $newStatus")
        }

        quotationFile.writeText(lines.joinToString("\n"))
        tvStatus.text = "Status: $newStatus"
        Toast.makeText(this, "Quotation $newStatus", Toast.LENGTH_SHORT).show()
    }
}
