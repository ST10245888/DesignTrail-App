package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import timber.log.Timber
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.databinding.ActivityAdminQuotationViewBinding
import vcmsa.projects.fkj_consultants.models.ChatMessage
import vcmsa.projects.fkj_consultants.models.Quotation
import vcmsa.projects.fkj_consultants.models.QuotationItem
import vcmsa.projects.fkj_consultants.utils.PdfGenerator
import java.text.SimpleDateFormat
import java.util.*

class AdminQuotationViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminQuotationViewBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var quotationId: String? = null
    private var currentQuotation: Quotation? = null
    private var quotationListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminQuotationViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeFirebase()
        setupToolbar()
        loadQuotationData()
        setupClickListeners()
        setupRealTimeListener()
    }

    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("quotations")
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Quotation Details"
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun loadQuotationData() {
        quotationId = intent.getStringExtra("QUOTATION_ID")
        if (quotationId == null) {
            showError("No quotation ID provided")
            finish()
            return
        }

        showLoading(true)
        Timber.d("Loading quotation with ID: $quotationId")
    }

    private fun setupRealTimeListener() {
        quotationListener = quotationId?.let { id ->
            database.child(id).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    showLoading(false)
                    if (!snapshot.exists()) {
                        showError("Quotation not found")
                        finish()
                        return
                    }

                    try {
                        currentQuotation = snapshot.getValue(Quotation::class.java)
                        currentQuotation?.id = snapshot.key ?: ""

                        if (currentQuotation != null) {
                            displayQuotationDetails(currentQuotation!!)
                            Timber.d("Quotation data loaded successfully")
                        } else {
                            showError("Error loading quotation data")
                        }
                    } catch (e: Exception) {
                        showError("Error parsing quotation data: ${e.message}")
                        Timber.e("Error parsing quotation: ${e.message}")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showLoading(false)
                    showError("Error loading quotation: ${error.message}")
                    Timber.e("Failed to load quotation: ${error.message}")
                }
            })
        }
    }

    private fun displayQuotationDetails(quotation: Quotation) {
        // Set company details
        binding.etCompanyName.setText(quotation.companyName)
        binding.etEmail.setText(quotation.email)
        binding.etPhone.setText(quotation.phone)
        binding.etAddress.setText(quotation.address)
        binding.etBillTo.setText(quotation.billTo)

        // Set quotation info - use the computed subtotal from the model
        binding.tvTotal.text = "R${"%.2f".format(quotation.subtotal)}"
        binding.tvStatus.text = quotation.status
        binding.tvDate.text = "Date: ${formatDate(quotation.timestamp)}"
        binding.tvServiceType.text = "Service: ${quotation.serviceType ?: "Not specified"}"

        // Set customer details if available
        if (quotation.customerName.isNotEmpty()) {
            binding.etCompanyName.setText("${quotation.companyName} (${quotation.customerName})")
        }
        if (quotation.customerEmail.isNotEmpty()) {
            binding.etEmail.setText(quotation.customerEmail)
        }

        // Update status color and style
        updateStatusColor(quotation.status)

        // Display products
        displayProducts(quotation.items)

        // Set admin notes if available
        if (quotation.adminNotes.isNotEmpty()) {
            binding.etAdminNotes.setText(quotation.adminNotes)
        }

        // Update button states based on status
        updateButtonStates(quotation.status)

        // Update toolbar title
        supportActionBar?.title = "Quotation from ${quotation.companyName}"

        // Show additional info if available
        displayAdditionalInfo(quotation)
    }

    private fun displayAdditionalInfo(quotation: Quotation) {
        // Display design file info if available
        quotation.designFileUrl?.let { designFileUrl ->
            if (designFileUrl.isNotEmpty()) {
                // You can add a button to view design file
                Timber.d("Design file available: $designFileUrl")
            }
        }

        // Display notes if available
        quotation.notes?.let { notes ->
            if (notes.isNotEmpty()) {
                // You can display customer notes in a separate section
                Timber.d("Customer notes: $notes")
            }
        }

        // Display admin actions timeline
        displayAdminTimeline(quotation)
    }

    private fun displayAdminTimeline(quotation: Quotation) {
        val timelineInfo = StringBuilder()

        if (quotation.approvedAt > 0) {
            timelineInfo.append("Approved on: ${formatDate(quotation.approvedAt)}\n")
        }
        if (quotation.rejectedAt > 0) {
            timelineInfo.append("Rejected on: ${formatDate(quotation.rejectedAt)}\n")
        }
        if (quotation.lastUpdated > 0 && quotation.lastUpdated != quotation.timestamp) {
            timelineInfo.append("Last updated: ${formatDate(quotation.lastUpdated)}\n")
        }
        if (quotation.adminId.isNotEmpty()) {
            timelineInfo.append("Admin ID: ${quotation.adminId}\n")
        }

        // You can display this info in a separate text view if needed
        if (timelineInfo.isNotEmpty()) {
            Timber.d("Admin timeline:\n$timelineInfo")
        }
    }

    private fun formatDate(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy 'at' HH:mm", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            "Unknown date"
        }
    }

    private fun displayProducts(items: List<QuotationItem>) {
        binding.productsContainer.removeAllViews()

        if (items.isEmpty()) {
            val emptyView = android.widget.TextView(this).apply {
                text = "No products in this quotation"
                setTextColor(ContextCompat.getColor(this@AdminQuotationViewActivity, R.color.textHint))
                setPadding(32, 32, 32, 32)
                textSize = 14f
                gravity = android.view.Gravity.CENTER
            }
            binding.productsContainer.addView(emptyView)
            return
        }

        // Add header
        val headerView = layoutInflater.inflate(R.layout.item_product_header, binding.productsContainer, false)
        binding.productsContainer.addView(headerView)

        // Calculate subtotal for display purposes only
        var displaySubtotal = 0.0

        // Add products with alternating background colors
        items.forEachIndexed { index, item ->
            val itemView = layoutInflater.inflate(R.layout.item_product_row, binding.productsContainer, false)

            // Calculate item total
            val itemTotal = item.pricePerUnit * item.quantity
            displaySubtotal += itemTotal

            // Set product data
            itemView.findViewById<android.widget.TextView>(R.id.txtProductName).text = item.name
            itemView.findViewById<android.widget.TextView>(R.id.txtQuantity).text = item.quantity.toString()
            itemView.findViewById<android.widget.TextView>(R.id.txtPrice).text = "R${"%.2f".format(item.pricePerUnit)}"
            itemView.findViewById<android.widget.TextView>(R.id.txtTotal).text = "R${"%.2f".format(itemTotal)}"

            // Set alternating background
            val backgroundColor = if (index % 2 == 0) {
                ContextCompat.getColor(this, R.color.backgroundLight)
            } else {
                ContextCompat.getColor(this, R.color.white)
            }
            itemView.setBackgroundColor(backgroundColor)

            binding.productsContainer.addView(itemView)
        }

        // Add subtotal row - use the displaySubtotal we calculated
        val subtotalView = layoutInflater.inflate(R.layout.item_product_row, binding.productsContainer, false)
        subtotalView.findViewById<android.widget.TextView>(R.id.txtProductName).apply {
            text = "Subtotal"
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@AdminQuotationViewActivity, R.color.textPrimary))
        }
        subtotalView.findViewById<android.widget.TextView>(R.id.txtQuantity).text = ""
        subtotalView.findViewById<android.widget.TextView>(R.id.txtPrice).text = ""
        subtotalView.findViewById<android.widget.TextView>(R.id.txtTotal).apply {
            text = "R${"%.2f".format(displaySubtotal)}"
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@AdminQuotationViewActivity, R.color.colorPrimary))
        }
        subtotalView.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_100))
        binding.productsContainer.addView(subtotalView)

        // Update total display - use the computed subtotal from the model
        // This ensures consistency with what's stored in Firebase
        binding.tvTotal.text = "R${"%.2f".format(currentQuotation?.subtotal ?: displaySubtotal)}"
    }

    private fun updateStatusColor(status: String) {
        val (color, background) = when (status) {
            Quotation.STATUS_PENDING -> Pair(
                ContextCompat.getColor(this, R.color.statusPending),
                R.drawable.bg_status_pending
            )
            Quotation.STATUS_APPROVED -> Pair(
                ContextCompat.getColor(this, R.color.statusApproved),
                R.drawable.bg_status_approved
            )
            Quotation.STATUS_REJECTED -> Pair(
                ContextCompat.getColor(this, R.color.statusRejected),
                R.drawable.bg_status_rejected
            )
            else -> {
                val fallbackColor = ContextCompat.getColor(this, R.color.textHint)
                val fallbackBackground = android.R.color.transparent
                Pair(fallbackColor, fallbackBackground)
            }
        }

        binding.tvStatus.setTextColor(color)
        try {
            binding.tvStatus.setBackgroundResource(background)
        } catch (e: Exception) {
            binding.tvStatus.setBackgroundColor(color)
        }
    }

    private fun updateButtonStates(status: String) {
        val isPending = status == Quotation.STATUS_PENDING

        // Show/hide action buttons based on status
        binding.btnApprove.visibility = if (isPending) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnReject.visibility = if (isPending) android.view.View.VISIBLE else android.view.View.GONE

        // Update button colors based on status
        when (status) {
            Quotation.STATUS_APPROVED -> {
                binding.btnApprove.setBackgroundColor(ContextCompat.getColor(this, R.color.buttonSuccess))
                binding.btnReject.setBackgroundColor(ContextCompat.getColor(this, R.color.buttonSuccess))
            }
            Quotation.STATUS_REJECTED -> {
                binding.btnApprove.setBackgroundColor(ContextCompat.getColor(this, R.color.buttonDanger))
                binding.btnReject.setBackgroundColor(ContextCompat.getColor(this, R.color.buttonDanger))
            }
            else -> {
                binding.btnApprove.setBackgroundColor(ContextCompat.getColor(this, R.color.buttonSuccess))
                binding.btnReject.setBackgroundColor(ContextCompat.getColor(this, R.color.buttonDanger))
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnApprove.setOnClickListener {
            currentQuotation?.let { approveQuotation(it) }
        }

        binding.btnReject.setOnClickListener {
            currentQuotation?.let { rejectQuotation(it) }
        }

        binding.btnDownloadPdf.setOnClickListener {
            currentQuotation?.let { downloadPdf(it) }
        }

        binding.btnSendEmail.setOnClickListener {
            currentQuotation?.let { sendEmail(it) }
        }

        binding.btnSaveNotes.setOnClickListener {
            saveAdminNotes()
        }
    }

    private fun approveQuotation(quotation: Quotation) {
        AlertDialog.Builder(this)
            .setTitle("Approve Quotation")
            .setMessage("Approve quotation from ${quotation.companyName}?")
            .setPositiveButton("Approve") { _, _ ->
                updateQuotationStatus(quotation, Quotation.STATUS_APPROVED)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun rejectQuotation(quotation: Quotation) {
        AlertDialog.Builder(this)
            .setTitle("Reject Quotation")
            .setMessage("Reject quotation from ${quotation.companyName}?")
            .setPositiveButton("Reject") { _, _ ->
                updateQuotationStatus(quotation, Quotation.STATUS_REJECTED)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateQuotationStatus(quotation: Quotation, newStatus: String) {
        val adminId = auth.currentUser?.uid ?: "unknown"
        val currentTime = System.currentTimeMillis()

        val updates = hashMapOf<String, Any>(
            "status" to newStatus,
            "lastUpdated" to currentTime,
            "adminId" to adminId
        )

        if (newStatus == Quotation.STATUS_APPROVED) {
            updates["approvedAt"] = currentTime
        } else if (newStatus == Quotation.STATUS_REJECTED) {
            updates["rejectedAt"] = currentTime
        }

        database.child(quotation.id).updateChildren(updates)
            .addOnSuccessListener {
                showSuccess(
                    when (newStatus) {
                        Quotation.STATUS_APPROVED -> "Quotation approved successfully!"
                        Quotation.STATUS_REJECTED -> "Quotation rejected successfully!"
                        else -> "Quotation updated successfully!"
                    }
                )

                // Send notification to customer
                sendStatusUpdateNotification(quotation, newStatus)

                Log.d(TAG, "Quotation status updated to: $newStatus")
            }
            .addOnFailureListener { e ->
                showError("Failed to update quotation: ${e.message}")
                Log.e(TAG, "Failed to update quotation status: ${e.message}")
            }
    }

    private fun sendStatusUpdateNotification(quotation: Quotation, newStatus: String) {
        try {
            val chatId = if (quotation.userId < "admin") {
                "${quotation.userId}_admin"
            } else {
                "admin_${quotation.userId}"
            }

            val chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId).child("messages")

            val message = ChatMessage(
                senderId = "admin",
                receiverId = quotation.userId,
                message = "Your quotation for ${quotation.companyName} has been $newStatus. Total: R${"%.2f".format(quotation.subtotal)}",
                timestamp = System.currentTimeMillis(),
                quotationId = quotation.id,
                type = ChatMessage.TYPE_STATUS_UPDATE
            )

            chatRef.push().setValue(message)
                .addOnSuccessListener {
                    showSuccess("Customer has been notified")
                    Log.d(TAG, "Status update notification sent to customer")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to send notification: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending notification: ${e.message}")
        }
    }

    private fun downloadPdf(quotation: Quotation) {
        try {
            showLoading(true)
            val pdfFile = PdfGenerator.generateQuotationPdf(this, quotation)
            showLoading(false)
            showSuccess("PDF generated: ${pdfFile.name}")

            val uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                pdfFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            try {
                startActivity(intent)
            } catch (e: Exception) {
                showError("No PDF viewer installed")
            }
        } catch (e: Exception) {
            showLoading(false)
            showError("Error generating PDF: ${e.message}")
            Log.e(TAG, "Error generating PDF: ${e.message}")
        }
    }

    private fun sendEmail(quotation: Quotation) {
        try {
            showLoading(true)
            val pdfFile = PdfGenerator.generateQuotationPdf(this, quotation)
            showLoading(false)

            val uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                pdfFile
            )

            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(quotation.email))
                putExtra(Intent.EXTRA_SUBJECT, "Quotation from FKJ Consultants")
                putExtra(Intent.EXTRA_TEXT, "Dear ${quotation.companyName},\n\nPlease find your quotation attached.\n\nStatus: ${quotation.status}\nTotal: R${"%.2f".format(quotation.subtotal)}\n\nBest regards,\nFKJ Consultants")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            try {
                startActivity(Intent.createChooser(emailIntent, "Send quotation via email"))
            } catch (e: Exception) {
                showError("No email app installed")
            }
        } catch (e: Exception) {
            showLoading(false)
            showError("Error sending email: ${e.message}")
            Log.e(TAG, "Error sending email: ${e.message}")
        }
    }

    private fun saveAdminNotes() {
        val notes = binding.etAdminNotes.text.toString().trim()
        currentQuotation?.let { quotation ->
            database.child(quotation.id).child("adminNotes").setValue(notes)
                .addOnSuccessListener {
                    showSuccess("Notes saved successfully")
                    currentQuotation?.adminNotes = notes
                    Log.d(TAG, "Admin notes saved for quotation: ${quotation.id}")
                }
                .addOnFailureListener { e ->
                    showError("Failed to save notes: ${e.message}")
                    Log.e(TAG, "Failed to save admin notes: ${e.message}")
                }
        } ?: showError("No quotation selected")
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        binding.scrollView.visibility = if (show) android.view.View.GONE else android.view.View.VISIBLE
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_quotation_view, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_refresh -> {
                loadQuotationData()
                showSuccess("Refreshing...")
                true
            }
            R.id.action_share -> {
                currentQuotation?.let { shareQuotation(it) }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun shareQuotation(quotation: Quotation) {
        try {
            showLoading(true)
            val pdfFile = PdfGenerator.generateQuotationPdf(this, quotation)
            showLoading(false)

            val uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Quotation from ${quotation.companyName}")
                putExtra(Intent.EXTRA_TEXT, "Quotation details for ${quotation.companyName}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Share Quotation"))
        } catch (e: Exception) {
            showLoading(false)
            showError("Error sharing quotation: ${e.message}")
            Log.e(TAG, "Error sharing quotation: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        quotationListener?.let {
            quotationId?.let { id -> database.child(id).removeEventListener(it) }
        }
        Log.d(TAG, "AdminQuotationViewActivity destroyed")
    }

    companion object {
        private const val TAG = "AdminQuotationView"
    }
}