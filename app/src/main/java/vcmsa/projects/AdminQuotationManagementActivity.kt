package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.adapters.AdminQuotationAdapter
import vcmsa.projects.fkj_consultants.databinding.ActivityAdminQuotationManagementBinding
import vcmsa.projects.fkj_consultants.models.ChatMessage
import vcmsa.projects.fkj_consultants.models.Quotation
import vcmsa.projects.fkj_consultants.utils.PdfGenerator

class AdminQuotationManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminQuotationManagementBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: AdminQuotationAdapter

    private val allQuotations = mutableListOf<Quotation>()
    private val filteredQuotations = mutableListOf<Quotation>()

    private var currentFilterStatus = "All"
    private var currentSortOption = "Date (Newest)"
    private var currentSearchQuery = ""
    private var quotationsListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminQuotationManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeFirebase()
        setupToolbar()
        setupRecyclerView()
        setupFilterSpinner()
        setupSortSpinner()
        setupSearchView()
        setupFirebaseListener()
    }

    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("quotations")
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Quotation Management"
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = AdminQuotationAdapter(
            quotations = filteredQuotations,
            onApproveClick = { approveQuotation(it) },
            onRejectClick = { rejectQuotation(it) },
            onViewClick = { viewQuotationDetails(it) },
            onDownloadClick = { downloadQuotation(it) }
        )

        binding.recyclerViewQuotations.apply {
            layoutManager = LinearLayoutManager(this@AdminQuotationManagementActivity)
            adapter = this@AdminQuotationManagementActivity.adapter
        }
    }

    private fun setupFilterSpinner() {
        val statusOptions = arrayOf("All", "Pending", "Approved", "Rejected")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilterStatus.adapter = adapter

        binding.spinnerFilterStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentFilterStatus = statusOptions[position]
                applyFiltersAndSort()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSortSpinner() {
        val sortOptions = arrayOf("Date (Newest)", "Date (Oldest)", "Company (A-Z)", "Company (Z-A)", "Total (High-Low)", "Total (Low-High)")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSort.adapter = adapter

        binding.spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentSortOption = sortOptions[position]
                applyFiltersAndSort()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSearchView() {
        binding.searchView.queryHint = "Search by company, email..."

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText?.trim() ?: ""
                applyFiltersAndSort()
                return true
            }
        })
    }

    private fun setupFirebaseListener() {
        binding.progressBar.visibility = View.VISIBLE

        quotationsListener = database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allQuotations.clear()
                snapshot.children.forEach { child ->
                    try {
                        val quotation = child.getValue(Quotation::class.java)
                        quotation?.id = child.key ?: ""
                        quotation?.let {
                            // Ensure customer details are populated
                            if (it.customerName.isEmpty()) {
                                it.customerName = it.companyName
                            }
                            if (it.customerEmail.isEmpty()) {
                                it.customerEmail = it.email
                            }
                            allQuotations.add(it)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing quotation ${child.key}: ${e.message}")
                    }
                }
                applyFiltersAndSort()
                updateEmptyState()
                binding.progressBar.visibility = View.GONE

                Log.d(TAG, "Loaded ${allQuotations.size} quotations from Firebase")
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
                showError("Failed to load quotations: ${error.message}")
                Log.e(TAG, "Failed to load quotations: ${error.message}")
            }
        })
    }

    private fun applyFiltersAndSort() {
        filteredQuotations.clear()

        // Apply status filter
        val statusFiltered = if (currentFilterStatus == "All") {
            allQuotations
        } else {
            allQuotations.filter {
                it.status.equals(currentFilterStatus, true)
            }
        }

        // Apply search filter
        val searchFiltered = if (currentSearchQuery.isEmpty()) statusFiltered
        else statusFiltered.filter {
            it.companyName.contains(currentSearchQuery, true) ||
                    it.customerName.contains(currentSearchQuery, true) ||
                    it.email.contains(currentSearchQuery, true) ||
                    it.serviceType?.contains(currentSearchQuery, true) == true ||
                    it.phone.contains(currentSearchQuery, true) ||
                    it.id.contains(currentSearchQuery, true)
        }

        // Apply sorting
        val sortedList = when (currentSortOption) {
            "Date (Newest)" -> searchFiltered.sortedByDescending { it.timestamp }
            "Date (Oldest)" -> searchFiltered.sortedBy { it.timestamp }
            "Company (A-Z)" -> searchFiltered.sortedBy { it.companyName.lowercase() }
            "Company (Z-A)" -> searchFiltered.sortedByDescending { it.companyName.lowercase() }
            "Total (High-Low)" -> searchFiltered.sortedByDescending { it.subtotal }
            "Total (Low-High)" -> searchFiltered.sortedBy { it.subtotal }
            else -> searchFiltered
        }

        filteredQuotations.addAll(sortedList)
        adapter.updateData(filteredQuotations)
        updateEmptyState()
        updateResultsCount()

        Log.d(TAG, "Applied filters: ${filteredQuotations.size} quotations after filtering")
    }

    private fun updateEmptyState() {
        if (filteredQuotations.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.recyclerViewQuotations.visibility = View.GONE

            binding.textEmptyMessage.text = when {
                allQuotations.isEmpty() -> "No quotations found"
                currentFilterStatus != "All" -> "No $currentFilterStatus quotations"
                currentSearchQuery.isNotEmpty() -> "No results for \"$currentSearchQuery\""
                else -> "No quotations match your criteria"
            }

            // Show/hide subtitle based on whether there are any results
            binding.textEmptySubtitle.visibility =
                if (allQuotations.isNotEmpty() && filteredQuotations.isEmpty()) View.VISIBLE else View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.recyclerViewQuotations.visibility = View.VISIBLE
        }
    }

    private fun updateResultsCount() {
        binding.textResultsCount.text = "Showing ${filteredQuotations.size} of ${allQuotations.size} quotations"
    }

    private fun approveQuotation(quotation: Quotation) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Approve Quotation")
            .setMessage("Are you sure you want to approve quotation from ${quotation.companyName}?")
            .setPositiveButton("Approve") { _, _ ->
                updateQuotationStatus(quotation, Quotation.STATUS_APPROVED)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun rejectQuotation(quotation: Quotation) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Reject Quotation")
            .setMessage("Are you sure you want to reject quotation from ${quotation.companyName}?")
            .setPositiveButton("Reject") { _, _ ->
                updateQuotationStatus(quotation, Quotation.STATUS_REJECTED)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateQuotationStatus(quotation: Quotation, newStatus: String) {
        if (!Quotation.isValidStatus(newStatus)) {
            showError("Invalid status: $newStatus")
            return
        }

        val adminId = auth.currentUser?.uid ?: "unknown"
        val currentTime = System.currentTimeMillis()

        val updates = hashMapOf<String, Any>(
            "status" to newStatus,
            "lastUpdated" to currentTime,
            "adminId" to adminId
        )

        // Update timestamps based on status
        when (newStatus) {
            Quotation.STATUS_APPROVED -> {
                updates["approvedAt"] = currentTime
                updates["rejectedAt"] = 0L
            }
            Quotation.STATUS_REJECTED -> {
                updates["rejectedAt"] = currentTime
                updates["approvedAt"] = 0L
            }
            Quotation.STATUS_PENDING -> {
                updates["approvedAt"] = 0L
                updates["rejectedAt"] = 0L
            }
        }

        database.child(quotation.id).updateChildren(updates)
            .addOnSuccessListener {
                val successMessage = when (newStatus) {
                    Quotation.STATUS_APPROVED -> "Quotation approved successfully!"
                    Quotation.STATUS_REJECTED -> "Quotation rejected successfully!"
                    else -> "Quotation updated successfully!"
                }
                showSuccess(successMessage)

                // Send real-time notification to customer
                sendStatusUpdateNotification(quotation, newStatus)

                Log.d(TAG, "Quotation ${quotation.id} status updated to: $newStatus")
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
                    Log.d(TAG, "Status update notification sent to customer")
                    showSuccess("Notification sent to customer")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to send notification: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending notification: ${e.message}")
        }
    }

    private fun viewQuotationDetails(quotation: Quotation) {
        val intent = Intent(this, AdminQuotationViewActivity::class.java)
        intent.putExtra("QUOTATION_ID", quotation.id)
        startActivity(intent)

        Log.d(TAG, "Opening quotation details for: ${quotation.id}")
    }

    private fun downloadQuotation(quotation: Quotation) {
        try {
            val pdfFile = PdfGenerator.generateQuotationPdf(this, quotation)
            showSuccess("PDF generated: ${pdfFile.name}")

            // Share the PDF
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                    this@AdminQuotationManagementActivity,
                    "${applicationContext.packageName}.fileprovider",
                    pdfFile
                ))
                putExtra(Intent.EXTRA_SUBJECT, "Quotation from ${quotation.companyName}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Share Quotation"))

            Log.d(TAG, "PDF generated for quotation: ${quotation.id}")
        } catch (e: Exception) {
            showError("Error generating PDF: ${e.message}")
            Log.e(TAG, "Error generating PDF: ${e.message}")
        }
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_admin_quotations, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_refresh -> {
                binding.progressBar.visibility = View.VISIBLE
                setupFirebaseListener()
                showSuccess("Refreshing...")
                true
            }
            R.id.action_export -> {
                exportQuotations()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun exportQuotations() {
        if (allQuotations.isEmpty()) {
            showError("No quotations to export")
            return
        }

        showSuccess("Exporting ${allQuotations.size} quotations...")

        try {
            exportQuotationsToCsv()
        } catch (e: Exception) {
            showError("Error exporting quotations: ${e.message}")
            Log.e(TAG, "Error exporting quotations: ${e.message}")
        }
    }

    private fun exportQuotationsToCsv() {
        val csvContent = StringBuilder()

        // Add header
        csvContent.append("ID,Company Name,Email,Phone,Status,Total,Date,Service Type\n")

        // Add data rows
        allQuotations.forEach { quotation ->
            csvContent.append("\"${quotation.id}\",")
            csvContent.append("\"${quotation.companyName}\",")
            csvContent.append("\"${quotation.email}\",")
            csvContent.append("\"${quotation.phone}\",")
            csvContent.append("\"${quotation.status}\",")
            csvContent.append("\"R${"%.2f".format(quotation.subtotal)}\",")
            csvContent.append("\"${quotation.formattedDate}\",")
            csvContent.append("\"${quotation.serviceType ?: "N/A"}\"\n")
        }

        try {
            val file = java.io.File(cacheDir, "quotations_export.csv")
            file.writeText(csvContent.toString())

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                    this@AdminQuotationManagementActivity,
                    "${applicationContext.packageName}.fileprovider",
                    file
                ))
                putExtra(Intent.EXTRA_SUBJECT, "Quotations Export")
                putExtra(Intent.EXTRA_TEXT, "Exported ${allQuotations.size} quotations")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Export Quotations"))

            showSuccess("Exported ${allQuotations.size} quotations")
        } catch (e: Exception) {
            showError("Error creating export file: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        quotationsListener?.let { database.removeEventListener(it) }
        Log.d(TAG, "AdminQuotationManagementActivity destroyed")
    }

    companion object {
        private const val TAG = "AdminQuotationManagement"
    }
}