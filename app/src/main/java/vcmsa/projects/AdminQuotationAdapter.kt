package vcmsa.projects.fkj_consultants.activities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.fkj_consultants.R

class AdminQuotationAdapter(
    private val quotations: List<Quotation>,
    private val onItemClick: (Quotation) -> Unit
) : RecyclerView.Adapter<AdminQuotationAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtCompany: TextView = view.findViewById(R.id.txtCompany)
        val txtService: TextView = view.findViewById(R.id.txtService)
        val txtStatus: TextView = view.findViewById(R.id.txtStatus)

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
        holder.txtCompany.text = quotation.companyName
        holder.txtService.text = quotation.serviceType
        holder.txtStatus.text = quotation.status

        holder.txtStatus.setTextColor(
            when (quotation.status) {
                "Approved" -> 0xFF2ECC71.toInt()
                "Rejected" -> 0xFFE74C3C.toInt()
                else -> 0xFF000000.toInt()
            }
        )
    }
}
