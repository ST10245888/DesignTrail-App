package vcmsa.projects.fkj_consultants.adapters

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.activities.QuotationViewerActivity
import vcmsa.projects.fkj_consultants.models.Quotation
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class QuotationAdapter(
    private val quotations: MutableList<Quotation>,
    private val currentUserId: String,
    private val receiverId: String,
    private val onSendClick: (Quotation, String, String) -> Unit
) : RecyclerView.Adapter<QuotationAdapter.QuotationViewHolder>() {

    inner class QuotationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtFileName: TextView = itemView.findViewById(R.id.txtFileName)
        private val txtTimestamp: TextView = itemView.findViewById(R.id.txtTimestamp)
        private val txtStatus: TextView = itemView.findViewById(R.id.txtStatus)
        private val txtSubtotal: TextView = itemView.findViewById(R.id.txtSubtotal)
        private val txtCompany: TextView = itemView.findViewById(R.id.txtCompany)
        private val btnView: Button = itemView.findViewById(R.id.btnView)
        private val btnDownload: Button = itemView.findViewById(R.id.btnDownload)
        private val btnSend: Button = itemView.findViewById(R.id.btnSend)
        private val btnDetails: Button = itemView.findViewById(R.id.btnDetails)

        fun bind(quotation: Quotation) {
            txtFileName.text = quotation.fileName.ifEmpty { "Unnamed File" }
            txtCompany.text = quotation.companyName.ifEmpty { "Unknown Company" }
            txtSubtotal.text = "R${"%.2f".format(quotation.subtotal)}"
            txtTimestamp.text = if (quotation.timestamp > 0) {
                val date = Date(quotation.timestamp)
                SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(date)
            } else "No date available"

            // Enhanced status color animation with fallback colors
            val previousColor = txtStatus.currentTextColor
            txtStatus.text = quotation.status

            val targetColor = when (quotation.status.lowercase(Locale.ROOT)) {
                "pending" -> ContextCompat.getColor(
                    itemView.context,
                    R.color.statusPending ?: android.R.color.holo_orange_dark
                )
                "approved" -> ContextCompat.getColor(
                    itemView.context,
                    R.color.statusApproved ?: android.R.color.holo_green_dark
                )
                "rejected" -> ContextCompat.getColor(
                    itemView.context,
                    R.color.statusRejected ?: android.R.color.holo_red_dark
                )
                else -> ContextCompat.getColor(
                    itemView.context,
                    R.color.textHint ?: android.R.color.darker_gray
                )
            }

            if (previousColor != targetColor) {
                ValueAnimator.ofObject(ArgbEvaluator(), previousColor, targetColor).apply {
                    duration = 600
                    addUpdateListener { animation ->
                        txtStatus.setTextColor(animation.animatedValue as Int)
                    }
                    start()
                }
            }

            // Set status background with fallback
            val statusBackground = when (quotation.status.lowercase(Locale.ROOT)) {
                "pending" -> R.drawable.bg_status_pending
                "approved" -> R.drawable.bg_status_approved
                "rejected" -> R.drawable.bg_status_rejected
                else -> R.drawable.bg_status_default
            }
            txtStatus.setBackgroundResource(statusBackground)

            // Click listeners with enhanced feedback
            btnView.setOnClickListener {
                animateButtonClick(btnView) { openFile(itemView.context, quotation) }
            }
            btnDownload.setOnClickListener {
                animateButtonClick(btnDownload) { downloadFile(itemView.context, quotation) }
            }
            btnSend.setOnClickListener {
                animateButtonClick(btnSend) { onSendClick(quotation, currentUserId, receiverId) }
            }
            btnDetails.setOnClickListener {
                animateButtonClick(btnDetails) {
                    val context = itemView.context
                    val intent = Intent(context, QuotationViewerActivity::class.java)
                    intent.putExtra("quotation", quotation)
                    context.startActivity(intent)
                }
            }

            // Update button states based on status
            updateButtonStates(quotation.status)
        }

        private fun animateButtonClick(button: Button, action: () -> Unit) {
            button.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    button.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                    action()
                }
                .start()
        }

        private fun updateButtonStates(status: String) {
            when (status.lowercase(Locale.ROOT)) {
                "approved" -> {
                    btnView.setBackgroundColor(ContextCompat.getColor(itemView.context,
                        R.color.buttonSuccess ?: android.R.color.holo_green_dark))
                    btnDownload.setBackgroundColor(ContextCompat.getColor(itemView.context,
                        R.color.buttonSuccess ?: android.R.color.holo_green_dark))
                    btnSend.setBackgroundColor(ContextCompat.getColor(itemView.context,
                        R.color.buttonSuccess ?: android.R.color.holo_green_dark))
                }
                "rejected" -> {
                    btnView.setBackgroundColor(ContextCompat.getColor(itemView.context,
                        R.color.buttonDanger ?: android.R.color.holo_red_dark))
                    btnDownload.setBackgroundColor(ContextCompat.getColor(itemView.context,
                        R.color.buttonDanger ?: android.R.color.holo_red_dark))
                    btnSend.setBackgroundColor(ContextCompat.getColor(itemView.context,
                        R.color.buttonDanger ?: android.R.color.holo_red_dark))
                }
                else -> {
                    btnView.setBackgroundColor(ContextCompat.getColor(itemView.context,
                        R.color.buttonPrimary ?: android.R.color.holo_blue_dark))
                    btnDownload.setBackgroundColor(ContextCompat.getColor(itemView.context,
                        R.color.blue_500 ?: android.R.color.holo_blue_light))
                    btnSend.setBackgroundColor(ContextCompat.getColor(itemView.context,
                        R.color.blue_100 ?: android.R.color.holo_orange_dark))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuotationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quotation, parent, false)
        return QuotationViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuotationViewHolder, position: Int) {
        holder.bind(quotations[position])
    }

    override fun getItemCount(): Int = quotations.size

    fun updateQuotations(newQuotations: List<Quotation>) {
        quotations.clear()
        quotations.addAll(newQuotations)
        notifyDataSetChanged()
    }

    private fun openFile(context: Context, quotation: Quotation) {
        val file = File(quotation.filePath)
        if (!file.exists()) {
            Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val mimeType = when (file.extension.lowercase(Locale.getDefault())) {
                "pdf" -> "application/pdf"
                "txt" -> "text/plain"
                "doc", "docx" -> "application/msword"
                "xls", "xlsx" -> "application/vnd.ms-excel"
                "ppt", "pptx" -> "application/vnd.ms-powerpoint"
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                else -> "*/*"
            }
            context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            })
        } catch (e: Exception) {
            Toast.makeText(context, "Error opening file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadFile(context: Context, quotation: Quotation) {
        val srcFile = File(quotation.filePath)
        if (!srcFile.exists()) {
            Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val destFile = File(downloadsDir, srcFile.name)
            FileInputStream(srcFile).use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            Toast.makeText(context, "File saved to Downloads: ${destFile.name}", Toast.LENGTH_LONG).show()

            // Optionally notify the system about the new file for MediaScanner
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = Uri.fromFile(destFile)
            context.sendBroadcast(mediaScanIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}