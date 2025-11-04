package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.ChatMessage
import vcmsa.projects.fkj_consultants.models.Quotation
import vcmsa.projects.fkj_consultants.models.QuotationItem
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class QuotationViewerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "QuotationViewerActivity"
        val ADMIN_EMAILS = listOf(
            "kush@gmail.com",
            "keitumetse01@gmail.com",
            "malikaOlivia@gmail.com",
            "JamesJameson@gmail.com"
        )
    }

    private lateinit var etCompanyName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etTel: EditText
    private lateinit var etAddress: EditText
    private lateinit var etBillTo: EditText
    private lateinit var tvStatus: TextView
    private lateinit var tvTotal: TextView
    private lateinit var productsContainer: LinearLayout
    private lateinit var btnViewQuotation: Button
    private lateinit var btnDownload: Button
    private lateinit var btnSendToAdmin: Button
    private lateinit var btnShare: Button

    private var currentQuotation: Quotation? = null
    private var quotationFile: File? = null

    private val currentUserEmail: String
        get() = FirebaseAuth.getInstance().currentUser?.email ?: "unknown@example.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quotation_view)

        setupViews()
        loadQuotationData()
        setupButtonListeners()
    }

    private fun setupViews() {
        etCompanyName = findViewById(R.id.etCompanyName)
        etEmail = findViewById(R.id.etEmail)
        etTel = findViewById(R.id.etTel)
        etAddress = findViewById(R.id.etAddress)
        etBillTo = findViewById(R.id.etBillTo)
        tvStatus = findViewById(R.id.tvStatus)
        tvTotal = findViewById(R.id.tvTotal)
        productsContainer = findViewById(R.id.productsContainer)
        btnViewQuotation = findViewById(R.id.btnViewQuotation)
        btnDownload = findViewById(R.id.btnDownload)
        btnSendToAdmin = findViewById(R.id.btnSendToAdmin)
        btnShare = findViewById(R.id.btnShare)

        // Make all fields read-only for viewing
        etCompanyName.isEnabled = false
        etEmail.isEnabled = false
        etTel.isEnabled = false
        etAddress.isEnabled = false
        etBillTo.isEnabled = false

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.quotation_details)
    }

    private fun loadQuotationData() {
        val quotation = intent.getParcelableExtra<Quotation>("quotation")
        if (quotation != null) {
            currentQuotation = quotation
            Log.d(TAG, "Loading quotation: ${quotation.id}")
            displayQuotationDetails(quotation)
        } else {
            Log.e(TAG, "No quotation passed to QuotationViewerActivity")
            Toast.makeText(this, "Error: No quotation data found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun displayQuotationDetails(quotation: Quotation) {
        // Set contact and status info
        etCompanyName.setText(quotation.companyName)
        etEmail.setText(quotation.email)
        etTel.setText(quotation.phone)
        etAddress.setText(quotation.address)
        etBillTo.setText(quotation.billTo)

        tvStatus.text = quotation.status
        tvTotal.text = "${getString(R.string.total_amount)}: R${"%.2f".format(quotation.subtotal)}"

        // Update status color with enhanced styling
        updateStatusColor(quotation.status)

        // Populate products with enhanced styling
        displayProducts(quotation.items)

        // Update title with company name
        supportActionBar?.title = "${getString(R.string.quotation_details)} - ${quotation.companyName}"
    }

    private fun displayProducts(items: List<QuotationItem>) {
        productsContainer.removeAllViews()

        if (items.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = getString(R.string.no_products)
                setTextColor(ContextCompat.getColor(this@QuotationViewerActivity, R.color.textHint))
                setPadding(16, 16, 16, 16)
                textSize = 14f
            }
            productsContainer.addView(emptyView)
            return
        }

        // Add header
        val headerView = TextView(this).apply {
            text = getString(R.string.product_details)
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@QuotationViewerActivity, R.color.textPrimary))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        }
        productsContainer.addView(headerView)

        items.forEachIndexed { index, item ->
            val productView = TextView(this).apply {
                text = "${index + 1}. ${item.name} x${item.quantity} @ R${"%.2f".format(item.pricePerUnit)} = R${"%.2f".format(item.total)}"
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@QuotationViewerActivity, R.color.textSecondary))
                setPadding(32, 12, 32, 12)
                background = ContextCompat.getDrawable(
                    this@QuotationViewerActivity,
                    if (index % 2 == 0) R.drawable.bg_item_even else R.drawable.bg_item_odd
                )
            }
            productsContainer.addView(productView)
        }
    }

    private fun updateStatusColor(status: String) {
        val color = when (status.lowercase()) {
            "pending" -> ContextCompat.getColor(this, R.color.statusPending)
            "approved" -> ContextCompat.getColor(this, R.color.statusApproved)
            "rejected" -> ContextCompat.getColor(this, R.color.statusRejected)
            else -> ContextCompat.getColor(this, R.color.textHint)
        }
        tvStatus.setTextColor(color)

        // Set status background
        val background = when (status.lowercase()) {
            "pending" -> R.drawable.bg_status_pending
            "approved" -> R.drawable.bg_status_approved
            "rejected" -> R.drawable.bg_status_rejected
            else -> R.drawable.bg_status_default
        }
        tvStatus.setBackgroundResource(background)

        // Add padding for better appearance
        tvStatus.setPadding(32, 16, 32, 16)
    }

    private fun setupButtonListeners() {
        btnViewQuotation.setOnClickListener { viewQuotation() }
        btnDownload.setOnClickListener { downloadQuotationPDF() }
        btnSendToAdmin.setOnClickListener { sendQuotationToAdmins() }
        btnShare.setOnClickListener { shareQuotation() }
    }

    /** ========================= VIEW QUOTATION ========================= */
    private fun viewQuotation() {
        currentQuotation?.let { quotation ->
            val text = createQuotationText(quotation)
            val tempFile = File(cacheDir, "quotation_view_${quotation.id}.txt")
            tempFile.writeText(text)

            val uri = FileProvider.getUriForFile(this, "${packageName}.provider", tempFile)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/plain")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(Intent.createChooser(intent, "View Quotation"))
        } ?: run {
            Toast.makeText(this, "No quotation data available", Toast.LENGTH_SHORT).show()
        }
    }

    /** ========================= PDF DOWNLOAD ========================= */
    private fun downloadQuotationPDF() {
        currentQuotation?.let { quotation ->
            try {
                val pdfFile = createQuotationPDF(quotation)
                if (pdfFile != null && pdfFile.exists()) {
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    if (!downloadsDir.exists()) downloadsDir.mkdirs()
                    val outFile = File(downloadsDir, pdfFile.name)
                    pdfFile.copyTo(outFile, overwrite = true)

                    Toast.makeText(this, "Quotation PDF saved to Downloads!", Toast.LENGTH_LONG).show()
                    Log.d(TAG, "PDF saved at: ${outFile.absolutePath}")

                    // Open the PDF
                    val pdfUri: Uri = FileProvider.getUriForFile(
                        this,
                        "${packageName}.provider",
                        outFile
                    )

                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(pdfUri, "application/pdf")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivity(Intent.createChooser(intent, "Open Quotation PDF"))
                } else {
                    Toast.makeText(this, "Failed to create PDF!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading quotation: ${e.message}", e)
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } ?: Toast.makeText(this, "No quotation data!", Toast.LENGTH_SHORT).show()
    }

    /** Creates PDF with professional styling */
    private fun createQuotationPDF(quotation: Quotation): File? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()

            var yPos = 50f
            val leftMargin = 40f
            val rightMargin = 555f

            // Header
            paint.textSize = 24f
            paint.color = Color.parseColor("#00796B")
            paint.isFakeBoldText = true
            canvas.drawText("FKJ CONSULTANTS", leftMargin, yPos, paint)
            yPos += 40f

            paint.textSize = 18f
            paint.color = Color.parseColor("#2C3E50")
            canvas.drawText("QUOTATION", leftMargin, yPos, paint)
            yPos += 30f

            // Date and Quotation Number
            paint.textSize = 12f
            paint.isFakeBoldText = false
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            canvas.drawText("Date: ${dateFormat.format(Date())}", leftMargin, yPos, paint)
            canvas.drawText("Quotation #: ${quotation.id}", rightMargin - 150f, yPos, paint)
            yPos += 30f

            // Separator line
            paint.strokeWidth = 2f
            canvas.drawLine(leftMargin, yPos, rightMargin, yPos, paint)
            yPos += 20f

            // Company Details
            paint.textSize = 14f
            paint.isFakeBoldText = true
            canvas.drawText("COMPANY DETAILS", leftMargin, yPos, paint)
            yPos += 20f

            paint.textSize = 11f
            paint.isFakeBoldText = false
            canvas.drawText("Company: ${quotation.companyName}", leftMargin, yPos, paint)
            yPos += 18f
            canvas.drawText("Email: ${quotation.email}", leftMargin, yPos, paint)
            yPos += 18f
            canvas.drawText("Phone: ${quotation.phone}", leftMargin, yPos, paint)
            yPos += 18f
            canvas.drawText("Address: ${quotation.address}", leftMargin, yPos, paint)
            yPos += 18f
            canvas.drawText("Bill To: ${quotation.billTo}", leftMargin, yPos, paint)
            yPos += 30f

            // Products Header
            paint.textSize = 14f
            paint.isFakeBoldText = true
            canvas.drawText("PRODUCTS/SERVICES", leftMargin, yPos, paint)
            yPos += 20f

            // Product Table Header
            paint.textSize = 10f
            paint.color = Color.parseColor("#00796B")
            canvas.drawText("Item", leftMargin, yPos, paint)
            canvas.drawText("Qty", leftMargin + 200f, yPos, paint)
            canvas.drawText("Price", leftMargin + 280f, yPos, paint)
            canvas.drawText("Total", leftMargin + 380f, yPos, paint)
            yPos += 15f

            paint.strokeWidth = 1f
            canvas.drawLine(leftMargin, yPos, rightMargin, yPos, paint)
            yPos += 15f

            // Product Items
            paint.color = Color.BLACK
            paint.isFakeBoldText = false
            quotation.items.forEach { item ->
                canvas.drawText(item.name, leftMargin, yPos, paint)
                canvas.drawText("${item.quantity}", leftMargin + 200f, yPos, paint)
                canvas.drawText("R${"%.2f".format(item.pricePerUnit)}", leftMargin + 280f, yPos, paint)
                canvas.drawText("R${"%.2f".format(item.total)}", leftMargin + 380f, yPos, paint)
                yPos += 18f
            }

            // Total
            yPos += 10f
            canvas.drawLine(leftMargin, yPos, rightMargin, yPos, paint)
            yPos += 20f

            paint.textSize = 14f
            paint.isFakeBoldText = true
            paint.color = Color.parseColor("#00796B")
            canvas.drawText("TOTAL: R${"%.2f".format(quotation.subtotal)}", rightMargin - 150f, yPos, paint)
            yPos += 30f

            // Status
            paint.textSize = 12f
            paint.color = when (quotation.status.lowercase()) {
                "approved" -> Color.parseColor("#4CAF50")
                "pending" -> Color.parseColor("#FF9800")
                else -> Color.parseColor("#F44336")
            }
            canvas.drawText("Status: ${quotation.status.uppercase()}", leftMargin, yPos, paint)

            // Footer
            yPos = 800f
            paint.textSize = 9f
            paint.color = Color.GRAY
            paint.isFakeBoldText = false
            canvas.drawText("Generated by FKJ Consultants System", leftMargin, yPos, paint)

            pdfDocument.finishPage(page)

            val fileName = "Quotation_${quotation.companyName.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
            val file = File(cacheDir, fileName)
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            file
        } catch (e: Exception) {
            Log.e(TAG, "Error creating PDF: ${e.message}", e)
            null
        }
    }

    /** ========================= SEND TO ADMINS ========================= */
    private fun sendQuotationToAdmins() {
        currentQuotation?.let { quotation ->
            try {
                btnSendToAdmin.isEnabled = false
                Toast.makeText(this, "Sending quotation to admin chat...", Toast.LENGTH_SHORT).show()

                // Create text version of quotation
                val quotationText = createQuotationText(quotation)

                // Save quotation text file
                val dir = File(getExternalFilesDir(null), "quotations")
                if (!dir.exists()) dir.mkdirs()

                val timestamp = System.currentTimeMillis()
                val fileName = "quotation_${quotation.id}_$timestamp.txt"
                val file = File(dir, fileName)
                file.writeText(quotationText)

                Log.d(TAG, "Quotation file saved: ${file.absolutePath}")

                // Send to unified chat
                val database = FirebaseDatabase.getInstance()
                val userEmail = currentUserEmail
                val chatId = encodeEmail(userEmail)
                val chatRoot = database.getReference("chats").child(chatId)
                val chatRef = chatRoot.child("messages")

                // Create chat message with attachment
                val message = ChatMessage(
                    senderId = userEmail,
                    receiverId = "admins",
                    message = "📄 Quotation #${quotation.id} - ${quotation.companyName} - Total: R${"%.2f".format(quotation.subtotal)}",
                    timestamp = timestamp,
                    attachmentUri = quotationText,
                    quotationId = quotation.id,
                    type = "quotation"
                )

                Log.d(TAG, "Pushing message to chat: $chatId")

                // Push message to unified chat
                chatRef.push().setValue(message)
                    .addOnSuccessListener {
                        Log.d(TAG, "Message pushed successfully")

                        // Update metadata for all admins
                        updateUnifiedChatMetadata(
                            chatRoot,
                            userEmail,
                            message.message,
                            timestamp
                        )

                        Toast.makeText(
                            this,
                            "✅ Quotation sent to admin chat successfully!",
                            Toast.LENGTH_LONG
                        ).show()

                        btnSendToAdmin.isEnabled = true
                        showSuccessDialog()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to send quotation: ${e.message}", e)
                        Toast.makeText(
                            this,
                            "❌ Error sending quotation: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        btnSendToAdmin.isEnabled = true
                    }

            } catch (e: Exception) {
                Log.e(TAG, "Exception in sendQuotationToAdmins: ${e.message}", e)
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                btnSendToAdmin.isEnabled = true
            }
        } ?: run {
            Toast.makeText(this, "No quotation data available", Toast.LENGTH_SHORT).show()
        }
    }

    /** ========================= SHARE QUOTATION ========================= */
    private fun shareQuotation() {
        currentQuotation?.let { quotation ->
            try {
                val text = createQuotationText(quotation)
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, text)
                    putExtra(Intent.EXTRA_SUBJECT, "Quotation from ${quotation.companyName}")
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(shareIntent, "Share Quotation"))
            } catch (e: Exception) {
                Toast.makeText(this, "Error sharing quotation: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** Show success dialog with option to view chat */
    private fun showSuccessDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Quotation Sent")
            .setMessage("Your quotation has been sent to the admin team. They will review it and respond in the chat.")
            .setPositiveButton("View Chat") { _, _ ->
                // Open chat activity
                val intent = Intent(this, ChatActivity::class.java)
                startActivity(intent)
            }
            .setNegativeButton("Close", null)
            .setCancelable(false)
            .show()
    }

    /** Create text representation of quotation */
    private fun createQuotationText(quotation: Quotation): String {
        val subtotal = quotation.items.sumOf { it.total }
        return buildString {
            appendLine("========== FKJ CONSULTANTS QUOTATION ==========")
            appendLine()
            appendLine("Quotation #: ${quotation.id}")
            appendLine("Company: ${quotation.companyName}")
            appendLine("Address: ${quotation.address}")
            appendLine("Email: ${quotation.email}")
            appendLine("Phone: ${quotation.phone}")
            appendLine("Bill To: ${quotation.billTo}")
            appendLine("Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(quotation.timestamp))}")
            appendLine("Status: ${quotation.status}")
            appendLine()
            appendLine("========== ITEMS ==========")
            appendLine()
            quotation.items.forEachIndexed { index, item ->
                appendLine("${index + 1}. ${item.name}")
                appendLine("   Quantity: ${item.quantity}")
                appendLine("   Price per unit: R${"%.2f".format(item.pricePerUnit)}")
                appendLine("   Total: R${"%.2f".format(item.total)}")
                appendLine()
            }
            appendLine("==============================")
            appendLine("SUBTOTAL: R${"%.2f".format(subtotal)}")
            appendLine("==============================")
            appendLine()
            appendLine("Thank you for your business!")
            appendLine("FKJ Consultants")
        }
    }

    /** Update metadata for unified chat */
    private fun updateUnifiedChatMetadata(
        chatRoot: com.google.firebase.database.DatabaseReference,
        userEmail: String,
        lastMessage: String,
        timestamp: Long
    ) {
        val metadataRef = chatRoot.child("metadata")

        val unreadMap = mutableMapOf<String, Any>()
        ADMIN_EMAILS.forEach { adminEmail ->
            unreadMap[encodeEmail(adminEmail)] = 1
        }

        val metadata = mapOf(
            "chatId" to encodeEmail(userEmail),
            "userEmail" to userEmail,
            "adminEmails" to ADMIN_EMAILS.map { encodeEmail(it) },
            "lastMessage" to lastMessage,
            "lastTimestamp" to timestamp,
            "lastSenderId" to userEmail,
            "unreadCounts" to unreadMap
        )

        metadataRef.updateChildren(metadata)
            .addOnSuccessListener {
                Log.d(TAG, "Chat metadata updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to update metadata: ${e.message}", e)
            }
    }

    private fun encodeEmail(email: String): String = email.replace(".", ",")

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        supportFinishAfterTransition()
    }
}