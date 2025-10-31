package vcmsa.projects.fkj_consultants.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.Quotation

class AdminQuotationAdapter(
    private val quotations: List<Quotation>,
    private val onItemClick: (Quotation) -> Unit
) : RecyclerView.Adapter<AdminQuotationAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtCompany: TextView = view.findViewById(R.id.txtCompany)
        val txtService: TextView = view.findViewById(R.id.txtService)
        val txtStatus: TextView = view.findViewById(R.id.txtStatus)
        val txtTotal: TextView = view.findViewById(R.id.txtTotal)

        init {
            view.setOnClickListener {
                onItemClick(quotations[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_quotation, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = quotations.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val quotation = quotations[position]

        // Safely handle nullable fields
        holder.txtCompany.text = quotation.companyName.ifEmpty { "Unknown Company" }
        holder.txtService.text = quotation.serviceType ?: "N/A"
        holder.txtStatus.text = quotation.status.ifEmpty { "Pending" }
        holder.txtTotal.text = "Total: R %.2f".format(quotation.subtotal)

        // Set status color dynamically
        holder.txtStatus.setTextColor(
            when (quotation.status.lowercase()) {
                "approved" -> 0xFF2ECC71.toInt()   // Green
                "rejected" -> 0xFFE74C3C.toInt()   // Red
                "pending" -> 0xFF9E9E9E.toInt()    // Gray
                else -> 0xFF000000.toInt()         // Black for unknown
            }
        )
    }
}
