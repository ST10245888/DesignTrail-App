package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.adapters.AdminQuotationAdapter
import vcmsa.projects.fkj_consultants.databinding.ActivityAdminQuotationListBinding
import vcmsa.projects.fkj_consultants.models.Quotation
import vcmsa.projects.fkj_consultants.utils.PdfGenerator
import java.util.*
import kotlin.collections.ArrayList

class AdminQuotationListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminQuotationListBinding
    private lateinit var adapter: AdminQuotationAdapter
    private val allQuotations = mutableListOf<Quotation>()
    private val filteredQuotations = mutableListOf<Quotation>()
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var currentStatusFilter = "All"
    private var currentSortOption = "Newest First"
    private var currentSearchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminQuotationListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeFirebase()
        setupToolbar()
        setupRecyclerView()
        setupFilterAndSort()
        setupSearchView()
        loadQuotationsFromFirebase()
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
            title = "Quotation Management"
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewQuotations.layoutManager = LinearLayoutManager(this)

        adapter = AdminQuotationAdapter(
            quotations = filteredQuotations,
            onApproveClick = { quotation ->
                updateQuotationStatus(quotation, Quotation.STATUS_APPROVED)
            },
            onRejectClick = { quotation ->
                updateQuotationStatus(quotation, Quotation.STATUS_REJECTED)
            },
            onViewClick = { quotation ->
                openQuotationDetails(quotation)
            },
            onDownloadClick = { quotation ->
                downloadQuotationPdf(quotation)
            }
        )
        binding.recyclerViewQuotations.adapter = adapter
    }

    private fun setupFilterAndSort() {
        // Status filter setup
        val statusOptions = arrayOf("All", "Pending", "Approved", "Rejected")
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusOptions)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilterStatus.adapter = statusAdapter

        binding.spinnerFilterStatus.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                currentStatusFilter = statusOptions[position]
                filterAndSortQuotations()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        // Sort options setup
        val sortOptions = arrayOf("Newest First", "Oldest First", "Total: High to Low", "Total: Low to High")
        val sortAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSort.adapter = sortAdapter

        binding.spinnerSort.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                currentSortOption = sortOptions[position]
                filterAndSortQuotations()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText?.trim() ?: ""
                filterAndSortQuotations()
                return true
            }
        })
    }

    private fun loadQuotationsFromFirebase() {
        showLoading(true)

        database.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allQuotations.clear()

                for (quotationSnapshot in snapshot.children) {
                    try {
                        val quotation = quotationSnapshot.getValue(Quotation::class.java)
                        quotation?.let {
                            it.id = quotationSnapshot.key ?: ""
                            allQuotations.add(it)
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@AdminQuotationListActivity,
                            "Error loading quotation: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                filterAndSortQuotations()
                showLoading(false)

                if (allQuotations.isEmpty()) {
                    showEmptyState(true, "No quotations found", "Create your first quotation to get started")
                } else {
                    showEmptyState(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showLoading(false)
                showEmptyState(true, "Error loading quotations", error.message)
                Toast.makeText(this@AdminQuotationListActivity,
                    "Failed to load quotations: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterAndSortQuotations() {
        filteredQuotations.clear()

        // Filter by status
        val statusFiltered = if (currentStatusFilter == "All") {
            allQuotations
        } else {
            allQuotations.filter { it.status == currentStatusFilter }
        }

        // Filter by search query
        val searchFiltered = if (currentSearchQuery.isEmpty()) {
            statusFiltered
        } else {
            statusFiltered.filter {
                it.companyName.contains(currentSearchQuery, true) ||
                        it.customerName.contains(currentSearchQuery, true) ||
                        it.email.contains(currentSearchQuery, true) ||
                        it.serviceType?.contains(currentSearchQuery, true) == true
            }
        }

        // Sort the results
        val sorted = when (currentSortOption) {
            "Newest First" -> searchFiltered.sortedByDescending { it.timestamp }
            "Oldest First" -> searchFiltered.sortedBy { it.timestamp }
            "Total: High to Low" -> searchFiltered.sortedByDescending { it.subtotal }
            "Total: Low to High" -> searchFiltered.sortedBy { it.subtotal }
            else -> searchFiltered.sortedByDescending { it.timestamp }
        }

        filteredQuotations.addAll(sorted)
        adapter.updateData(ArrayList(filteredQuotations))

        // Update results count
        binding.textResultsCount.text = "Showing ${filteredQuotations.size} of ${allQuotations.size} quotations"

        // Show/hide empty state
        if (filteredQuotations.isEmpty()) {
            val message = if (allQuotations.isEmpty()) {
                "No quotations found"
            } else {
                "No quotations match your search"
            }
            showEmptyState(true, message, "Try adjusting your filters or search")
        } else {
            showEmptyState(false)
        }
    }

    private fun updateQuotationStatus(quotation: Quotation, newStatus: String) {
        val adminId = auth.currentUser?.uid ?: "unknown"
        val currentTime = System.currentTimeMillis()

        val updates = hashMapOf<String, Any>(
            "status" to newStatus,
            "lastUpdated" to currentTime,
            "adminId" to adminId
        )

        when (newStatus) {
            Quotation.STATUS_APPROVED -> updates["approvedAt"] = currentTime
            Quotation.STATUS_REJECTED -> updates["rejectedAt"] = currentTime
        }

        database.child(quotation.id).updateChildren(updates)
            .addOnSuccessListener {
                // Update local quotation object
                quotation.status = newStatus
                quotation.lastUpdated = currentTime
                quotation.adminId = adminId

                if (newStatus == Quotation.STATUS_APPROVED) {
                    quotation.approvedAt = currentTime
                } else if (newStatus == Quotation.STATUS_REJECTED) {
                    quotation.rejectedAt = currentTime
                }

                filterAndSortQuotations()
                Toast.makeText(this, "Quotation $newStatus", Toast.LENGTH_SHORT).show()

                sendStatusUpdateNotification(quotation, newStatus)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating quotation: ${e.message}", Toast.LENGTH_SHORT).show()
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

            val message = vcmsa.projects.fkj_consultants.models.ChatMessage(
                senderId = "admin",
                receiverId = quotation.userId,
                message = "Your quotation for ${quotation.companyName} has been $newStatus. Total: R${"%.2f".format(quotation.subtotal)}",
                timestamp = System.currentTimeMillis(),
                quotationId = quotation.id,
                type = "status_update"
            )

            chatRef.push().setValue(message)
        } catch (e: Exception) {
            // Silent fail for notifications
        }
    }

    private fun openQuotationDetails(quotation: Quotation) {
        val intent = Intent(this, AdminQuotationViewActivity::class.java)
        intent.putExtra("QUOTATION_ID", quotation.id)
        startActivity(intent)
    }

    private fun downloadQuotationPdf(quotation: Quotation) {
        try {
            showLoading(true)
            val pdfFile = PdfGenerator.generateQuotationPdf(this, quotation)
            showLoading(false)

            Toast.makeText(this, "PDF generated: ${pdfFile.name}", Toast.LENGTH_SHORT).show()

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(
                    this@AdminQuotationListActivity,
                    "${applicationContext.packageName}.fileprovider",
                    pdfFile
                ))
                putExtra(Intent.EXTRA_SUBJECT, "Quotation from ${quotation.companyName}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Share Quotation"))
        } catch (e: Exception) {
            showLoading(false)
            Toast.makeText(this, "Error generating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun showEmptyState(show: Boolean, title: String = "", subtitle: String = "") {
        if (show) {
            binding.emptyState.visibility = android.view.View.VISIBLE
            binding.recyclerViewQuotations.visibility = android.view.View.GONE
            binding.textEmptyMessage.text = title
            binding.textEmptySubtitle.text = subtitle
            binding.textEmptySubtitle.visibility = if (subtitle.isNotEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        } else {
            binding.emptyState.visibility = android.view.View.GONE
            binding.recyclerViewQuotations.visibility = android.view.View.VISIBLE
        }
    }

    // Remove the menu methods since we're handling everything in the UI
    // override fun onCreateOptionsMenu(menu: Menu): Boolean {
    //     menuInflater.inflate(R.menu.menu_quotation_list, menu)
    //     return true
    // }
    //
    // override fun onOptionsItemSelected(item: MenuItem): Boolean {
    //     return when (item.itemId) {
    //         android.R.id.home -> {
    //             onBackPressed()
    //             true
    //         }
    //         R.id.action_refresh -> {
    //             loadQuotationsFromFirebase()
    //             Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show()
    //             true
    //         }
    //         else -> super.onOptionsItemSelected(item)
    //     }
    // }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up any listeners if needed
    }
}