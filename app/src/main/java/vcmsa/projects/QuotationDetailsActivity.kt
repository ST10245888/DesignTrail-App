package vcmsa.projects.fkj_consultants.activities

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.Quotation
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class QuotationDetailsActivity : AppCompatActivity() {

    private val nf = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quotation_details)

        val q = intent.getParcelableExtra<Quotation>("quotation")

        val tvMeta: TextView = findViewById(R.id.tvMeta)
        val tvClient: TextView = findViewById(R.id.tvClient)
        val tvTotal: TextView = findViewById(R.id.tvTotal)
        val recycler: RecyclerView = findViewById(R.id.recyclerDetailsItems)

        recycler.layoutManager = LinearLayoutManager(this)

        if (q != null) {
            // Convert BasketItems to MaterialListItems for display
            val materialListItems = q.products.map { vcmsa.projects.fkj_consultants.models.MaterialListItem.MaterialEntry(it.material) }

            recycler.adapter = vcmsa.projects.fkj_consultants.adapters.MaterialAdapter(
                materialListItems,
                this
            ) { _, _, _, _ -> /* read-only */ }

            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(q.timestamp))
            tvMeta.text = "Quote ID: ${q.quotationId}  •  Date: $dateStr"
            tvClient.text = "${q.companyName}  —  ${q.requesterName}\n${q.email} • ${q.tel}\n${q.address}\nBill To: ${q.billTo}"
            tvTotal.text = "Total: ${nf.format(q.total)}"
        } else {
            tvMeta.text = "No details available for this quotation"
        }
    }
}
