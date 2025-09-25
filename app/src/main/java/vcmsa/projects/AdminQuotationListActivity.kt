package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.fkj_consultants.R
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
            intent.putExtra("quotationFilePath", quotation.filePath) // pass the txt file path
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        loadQuotations()
    }

    private fun loadQuotations() {
        val dir = File(getExternalFilesDir(null), "quotations") // Folder where txt files are stored
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
            var subtotal = 0.0
            var status = "Pending"
            var serviceType = ""
            var userId = ""

            for (line in lines) {
                when {
                    line.startsWith("Company:") -> company = line.substringAfter("Company:").trim()
                    line.startsWith("Address:") -> address = line.substringAfter("Address:").trim()
                    line.startsWith("Email:") -> email = line.substringAfter("Email:").trim()
                    line.startsWith("Phone:") -> phone = line.substringAfter("Phone:").trim()
                    line.startsWith("BillTo:") -> billTo = line.substringAfter("BillTo:").trim()
                    line.startsWith("Subtotal:") -> subtotal = line.substringAfter("Subtotal:").trim().toDoubleOrNull() ?: 0.0
                    line.startsWith("Status:") -> status = line.substringAfter("Status:").trim()
                    line.startsWith("ServiceType:") -> serviceType = line.substringAfter("ServiceType:").trim()
                    line.startsWith("UserId:") -> userId = line.substringAfter("UserId:").trim()
                }
            }

            quotations.add(
                Quotation(
                    id = file.name, // we can use filename as id
                    userId = userId,
                    companyName = company,
                    address = address,
                    email = email,
                    phone = phone,
                    billTo = billTo,
                    subtotal = subtotal,
                    status = status,
                    serviceType = serviceType,
                    filePath = file.absolutePath
                )
            )
        }

        adapter.notifyDataSetChanged()
    }
}
