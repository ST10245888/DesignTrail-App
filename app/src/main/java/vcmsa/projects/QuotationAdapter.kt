package vcmsa.projects.fkj_consultants.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.Quotation

class QuotationAdapter(
    private val quotations: List<Quotation>,
    private val onItemClick: (Quotation) -> Unit
) : RecyclerView.Adapter<QuotationAdapter.QuotationViewHolder>() {

    class QuotationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCompany: TextView = itemView.findViewById(R.id.tvCompany)
        val tvTotal: TextView = itemView.findViewById(R.id.tvTotal)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuotationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quotation, parent, false)
        return QuotationViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuotationViewHolder, position: Int) {
        val quotation = quotations[position]
        holder.tvCompany.text = quotation.companyName
        holder.tvTotal.text = "Total: R ${quotation.total}"
        holder.tvStatus.text = "Status: ${quotation.status}"
        holder.itemView.setOnClickListener { onItemClick(quotation) }
    }

    override fun getItemCount() = quotations.size
}
