package vcmsa.projects.fkj_consultants.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.BasketItem
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
            recycler.adapter = BasketAdapter(q.products)

            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(q.timestamp))
            tvMeta.text = "Quote ID: ${q.quotationId}  •  Date: $dateStr"
            tvClient.text = "${q.companyName} — ${q.requesterName}\n${q.email} • ${q.tel}\n${q.address}\nBill To: ${q.billTo}"
            tvTotal.text = "Total: ${nf.format(q.total)}"
        } else {
            tvMeta.text = "No details available for this quotation"
        }
    }

    // Adapter for displaying BasketItems
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
            val product = item.product // assuming BasketItem now uses Product instead of MaterialItem

            holder.tvName.text = product.name
            holder.tvDetails.text = "Qty: ${item.quantity}, Color: ${product.color}, Size: ${product.size}"
            holder.tvPrice.text = "R %.2f".format(product.price * item.quantity)
        }
    }
}
