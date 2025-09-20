package vcmsa.projects.fkj_consultants.activities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.fkj_consultants.R

class QuotationAdapter(
    private val quotations: List<Quotation>,
    private val onItemClick: (Quotation) -> Unit
) : RecyclerView.Adapter<QuotationAdapter.QuotationViewHolder>() {

    class QuotationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtFileName: TextView = itemView.findViewById(R.id.txtFileName)
        val txtTimestamp: TextView = itemView.findViewById(R.id.txtTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuotationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_quotation, parent, false)
        return QuotationViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuotationViewHolder, position: Int) {
        val quotation = quotations[position]
        holder.txtFileName.text = quotation.fileName
        holder.txtTimestamp.text = "Saved at: ${quotation.timestamp}"

        holder.itemView.setOnClickListener { onItemClick(quotation) }
    }

    override fun getItemCount(): Int = quotations.size
}
