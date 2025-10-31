package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.adapters.AdminQuotationAdapter
import vcmsa.projects.fkj_consultants.models.Quotation
import java.io.File

class AdminQuotationListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminQuotationAdapter
    private val quotations = mutableListOf<Quotation>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_quotation_list)

        recyclerView = findViewById(R.id.recyclerViewQuotations)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = AdminQuotationAdapter(quotations) { quotation ->
            // Open quotation details activity
            val intent = Intent(this, AdminQuotationViewActivity::class.java)
            intent.putExtra("quotationFilePath", quotation.filePath)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        loadQuotations()
    }

    private fun loadQuotations() {
        val dir = File(getExternalFilesDir(null), "quotations")
        if (!dir.exists()) {
            Toast.makeText(this, "No quotations found", Toast.LENGTH_SHORT).show()
            return
        }

        val files = dir.listFiles { _, name -> name.endsWith(".txt") } ?: return
        quotations.clear()

        for (file in files) {
            val lines = file.readLines()
            var company = ""
            var address = ""
            var email = ""
            var phone = ""
            var billTo = ""
            var status = "Pending"
            var serviceType = ""
            var userId = ""
            val items = mutableListOf<vcmsa.projects.fkj_consultants.models.QuotationItem>()

            for (line in lines) {
                when {
                    line.startsWith("Company:") -> company = line.substringAfter("Company:").trim()
                    line.startsWith("Address:") -> address = line.substringAfter("Address:").trim()
                    line.startsWith("Email:") -> email = line.substringAfter("Email:").trim()
                    line.startsWith("Phone:") -> phone = line.substringAfter("Phone:").trim()
                    line.startsWith("BillTo:") -> billTo = line.substringAfter("BillTo:").trim()
                    line.startsWith("Status:") -> status = line.substringAfter("Status:").trim()
                    line.startsWith("ServiceType:") -> serviceType = line.substringAfter("ServiceType:").trim()
                    line.startsWith("UserId:") -> userId = line.substringAfter("UserId:").trim()
                    line.startsWith("Item:") -> {
                        // Optional: parse item lines if you saved them like "Item:name,price,quantity,productId"
                        val parts = line.substringAfter("Item:").split(",")
                        if (parts.size >= 4) {
                            val item = vcmsa.projects.fkj_consultants.models.QuotationItem(
                                name = parts[0].trim(),
                                pricePerUnit = parts[1].trim().toDoubleOrNull() ?: 0.0,
                                quantity = parts[2].trim().toIntOrNull() ?: 0,
                                productId = parts[3].trim()
                            )
                            items.add(item)
                        }
                    }
                }
            }

            quotations.add(
                Quotation(
                    id = file.name,
                    userId = userId,
                    companyName = company,
                    address = address,
                    email = email,
                    phone = phone,
                    billTo = billTo,
                    status = status,
                    serviceType = serviceType,
                    filePath = file.absolutePath,
                    items = items // subtotal will be computed automatically
                )
            )
        }

        adapter.notifyDataSetChanged()
    }
}
