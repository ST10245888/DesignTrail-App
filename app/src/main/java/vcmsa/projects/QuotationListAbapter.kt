package vcmsa.projects.fkj_consultants.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.Quotation
import java.text.SimpleDateFormat
import java.util.*

class QuotationListAdapter(
    private val quotations: List<Quotation>,
    private val onClick: (Quotation) -> Unit
) : RecyclerView.Adapter<QuotationListAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCompany: TextView = view.findViewById(R.id.tvCompany)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvServiceType: TextView = view.findViewById(R.id.tvServiceType)

        init {
            // Set click listener for the entire item
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onClick(quotations[adapterPosition])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quotation_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val quotation = quotations[position]

        // Set company name
        holder.tvCompany.text = quotation.companyName.ifEmpty { "Unknown Company" }

        // Set status with color and background
        holder.tvStatus.text = quotation.status.uppercase()
        setStatusStyle(holder.tvStatus, quotation.status)

        // Set formatted date
        holder.tvDate.text = formatDate(quotation.timestamp)

        // Set amount
        holder.tvAmount.text = formatAmount(quotation.subtotal)

        // Set service type
        holder.tvServiceType.text = quotation.serviceType ?: "General Service"
    }

    override fun getItemCount(): Int = quotations.size

    /**
     * Updates the dataset and refreshes the adapter
     */
    fun updateQuotations(newQuotations: List<Quotation>) {
        (this.quotations as? MutableList)?.let { mutableList ->
            mutableList.clear()
            mutableList.addAll(newQuotations)
            notifyDataSetChanged()
        }
    }

    /**
     * Sets the appropriate text color and background for the status
     */
    private fun setStatusStyle(textView: TextView, status: String) {
        val (backgroundColor, textColor) = when (status.lowercase(Locale.ROOT)) {
            "pending" -> Pair("#FFF4E6", "#F39C12") // Light orange background, orange text
            "approved" -> Pair("#E8F6EF", "#27AE60") // Light green background, green text
            "rejected" -> Pair("#FDEDEC", "#E74C3C") // Light red background, red text
            "completed" -> Pair("#EBF5FB", "#2980B9") // Light blue background, blue text
            "cancelled" -> Pair("#F4F6F6", "#95A5A6") // Light gray background, gray text
            else -> Pair("#F8F9F9", "#7F8C8D") // Default light background, gray text
        }

        textView.setBackgroundColor(Color.parseColor(backgroundColor))
        textView.setTextColor(Color.parseColor(textColor))

        // Add padding and corner radius programmatically
        textView.setPadding(
            dipToPx(textView.context, 8f),
            dipToPx(textView.context, 4f),
            dipToPx(textView.context, 8f),
            dipToPx(textView.context, 4f)
        )

        // Set corner radius
        textView.background = createRoundedDrawable(Color.parseColor(backgroundColor))
    }

    /**
     * Creates a rounded rectangle drawable for status background
     */
    private fun createRoundedDrawable(color: Int): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            setColor(color)
            cornerRadius = dipToPx(null, 4f).toFloat()
        }
    }

    /**
     * Conits dip to pixels
     */
    private fun dipToPx(context: android.content.Context?, dip: Float): Int {
        return if (context != null) {
            (dip * context.resources.displayMetrics.density).toInt()
        } else {
            (dip * 3f).toInt() // Fallback
        }
    }

    /**
     * Formats the amount with currency symbol
     */
    private fun formatAmount(amount: Double): String {
        return "R${"%.2f".format(amount)}"
    }

    /**
     * Formats timestamp to readable date
     */
    private fun formatDate(timestamp: Long): String {
        return if (timestamp > 0) {
            try {
                val date = Date(timestamp)
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
            } catch (e: Exception) {
                "Invalid Date"
            }
        } else {
            "No Date"
        }
    }

    /**
     * Gets the quotation at specified position
     */
    fun getQuotationAt(position: Int): Quotation? {
        return if (position in 0 until itemCount) {
            quotations[position]
        } else {
            null
        }
    }

    /**
     * Filters quotations by status
     */
    fun filterByStatus(status: String): List<Quotation> {
        return quotations.filter { it.status.equals(status, ignoreCase = true) }
    }

    /**
     * Filters quotations by company name
     */
    fun filterByCompany(companyName: String): List<Quotation> {
        return quotations.filter {
            it.companyName.contains(companyName, ignoreCase = true)
        }
    }

    /**
     * Gets all unique statuses from quotations
     */
    fun getUniqueStatuses(): List<String> {
        return quotations.map { it.status }.distinct()
    }

    /**
     * Gets quotations count by status
     */
    fun getQuotationCountByStatus(status: String): Int {
        return quotations.count { it.status.equals(status, ignoreCase = true) }
    }

    companion object {
        /**
         * Creates an adapter with empty list
         */
        fun createEmpty(onClick: (Quotation) -> Unit): QuotationListAdapter {
            return QuotationListAdapter(emptyList(), onClick)
        }
    }
}