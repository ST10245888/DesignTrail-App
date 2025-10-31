package vcmsa.projects.fkj_consultants.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.Quotation
import java.io.File

class QuotationListAdapter(
    private val quotations: List<Quotation>,
    private val onClick: (Quotation) -> Unit
) : RecyclerView.Adapter<QuotationListAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCompany: TextView = view.findViewById(R.id.tvCompany)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quotation_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val quotation = quotations[position]
        holder.tvCompany.text = quotation.companyName
        holder.tvStatus.text = quotation.status

        // Status color
        when (quotation.status) {
            "Pending" -> holder.tvStatus.setTextColor(Color.parseColor("#F1C40F")) // Yellow
            "Approved" -> holder.tvStatus.setTextColor(Color.parseColor("#2ECC71")) // Green
            "Rejected" -> holder.tvStatus.setTextColor(Color.parseColor("#E74C3C")) // Red
        }

        holder.itemView.setOnClickListener {
            onClick(quotation)
        }
    }

    override fun getItemCount(): Int = quotations.size
}
