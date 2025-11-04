package vcmsa.projects.fkj_consultants.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.Quotation
import java.text.SimpleDateFormat
import java.util.*

class AdminQuotationAdapter(
    private var quotations: List<Quotation>,
    private val onApproveClick: (Quotation) -> Unit,
    private val onRejectClick: (Quotation) -> Unit,
    private val onViewClick: (Quotation) -> Unit,
    private val onDownloadClick: (Quotation) -> Unit
) : RecyclerView.Adapter<AdminQuotationAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtCompany: TextView = itemView.findViewById(R.id.txtCompany)
        val txtCustomer: TextView = itemView.findViewById(R.id.txtCustomer)
        val txtService: TextView = itemView.findViewById(R.id.txtService)
        val txtTotal: TextView = itemView.findViewById(R.id.txtTotal)
        val txtStatus: TextView = itemView.findViewById(R.id.txtStatus)
        val txtDate: TextView = itemView.findViewById(R.id.txtDate)
        val txtItemsCount: TextView = itemView.findViewById(R.id.txtItemsCount)
        val btnApprove: Button = itemView.findViewById(R.id.btnApprove)
        val btnReject: Button = itemView.findViewById(R.id.btnReject)
        val btnView: Button = itemView.findViewById(R.id.btnView)
        val btnDownload: Button = itemView.findViewById(R.id.btnDownload)

        // Additional views for enhanced UI
        val cardView: View = itemView.findViewById(R.id.cardView)
        val statusIndicator: View = itemView.findViewById(R.id.statusIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_quotation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val quotation = quotations[position]

        // Set basic quotation information
        holder.txtCompany.text = quotation.companyName.ifEmpty { "Unknown Company" }
        holder.txtCustomer.text = quotation.customerName.ifEmpty { quotation.email }
        holder.txtService.text = quotation.serviceType ?: "General Service"
        holder.txtTotal.text = "R${"%.2f".format(quotation.subtotal)}"
        holder.txtItemsCount.text = "${quotation.items.size} items"
        holder.txtDate.text = formatDate(quotation.timestamp)

        // Set status with enhanced styling
        holder.txtStatus.text = quotation.status.uppercase()
        updateStatusUI(holder, quotation.status)

        // Set click listeners
        holder.btnApprove.setOnClickListener { onApproveClick(quotation) }
        holder.btnReject.setOnClickListener { onRejectClick(quotation) }
        holder.btnView.setOnClickListener { onViewClick(quotation) }
        holder.btnDownload.setOnClickListener { onDownloadClick(quotation) }

        // Add item click listener for the whole card
        holder.itemView.setOnClickListener {
            onViewClick(quotation)
        }
    }

    private fun updateStatusUI(holder: ViewHolder, status: String) {
        when (status) {
            Quotation.STATUS_PENDING -> {
                // Status text styling
                holder.txtStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.quotation_pending_text))
                holder.txtStatus.setBackgroundResource(R.drawable.bg_status_pending)

                // Status indicator
                holder.statusIndicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.statusPending))

                // Button visibility and styling
                holder.btnApprove.visibility = View.VISIBLE
                holder.btnReject.visibility = View.VISIBLE
                holder.btnApprove.isEnabled = true
                holder.btnReject.isEnabled = true

                // Set button colors for pending status
                holder.btnApprove.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.buttonSuccess))
                holder.btnReject.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.buttonDanger))
                holder.btnView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.buttonPrimary))
                holder.btnDownload.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.blue_500))

                // Card background for pending
                holder.cardView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.quotation_pending_bg))
            }
            Quotation.STATUS_APPROVED -> {
                // Status text styling
                holder.txtStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.quotation_approved_text))
                holder.txtStatus.setBackgroundResource(R.drawable.bg_status_approved)

                // Status indicator
                holder.statusIndicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.statusApproved))

                // Button visibility and styling
                holder.btnApprove.visibility = View.GONE
                holder.btnReject.visibility = View.GONE

                // Set button colors for approved status
                holder.btnView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.buttonSuccess))
                holder.btnDownload.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.buttonSuccess))

                // Card background for approved
                holder.cardView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.statusRejected))
            }
            Quotation.STATUS_REJECTED -> {
                // Status text styling
                holder.txtStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.statusRejected))
                holder.txtStatus.setBackgroundResource(R.drawable.bg_status_rejected)

                // Status indicator
                holder.statusIndicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.statusRejected))

                // Button visibility and styling
                holder.btnApprove.visibility = View.GONE
                holder.btnReject.visibility = View.GONE

                // Set button colors for rejected status
                holder.btnView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.buttonDanger))
                holder.btnDownload.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.buttonDanger))

                // Card background for rejected
                holder.cardView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.quotation_rejected_bg))
            }
            else -> {
                // Fallback for unknown status
                holder.txtStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.textSecondary))
                holder.txtStatus.setBackgroundResource(R.drawable.bg_status_pending)
                holder.statusIndicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.gray_500))
                holder.btnApprove.visibility = View.GONE
                holder.btnReject.visibility = View.GONE
                holder.cardView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.cardBackground))
            }
        }

        // Set text colors for buttons
        holder.btnApprove.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.buttonSuccessText))
        holder.btnReject.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.buttonDangerText))
        holder.btnView.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.buttonPrimaryText))
        holder.btnDownload.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.buttonPrimaryText))
    }

    private fun formatDate(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            "Unknown date"
        }
    }

    override fun getItemCount(): Int = quotations.size

    fun updateData(newQuotations: List<Quotation>) {
        quotations = newQuotations
        notifyDataSetChanged()
    }

    fun updateQuotationStatus(quotationId: String, newStatus: String) {
        val position = quotations.indexOfFirst { it.id == quotationId }
        if (position != -1) {
            quotations[position].status = newStatus
            notifyItemChanged(position)
        }
    }

    fun getQuotationAt(position: Int): Quotation {
        return quotations[position]
    }

    fun clearData() {
        (quotations as? MutableList)?.clear()
        notifyDataSetChanged()
    }
}