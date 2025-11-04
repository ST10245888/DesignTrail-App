package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.Quotation
import vcmsa.projects.fkj_consultants.models.QuotationItem
import vcmsa.projects.fkj_consultants.utils.PdfGenerator
import java.io.File
import java.util.*

class UserQuotationViewActivity : AppCompatActivity() {

    // Declare UI components (Android Developers, 2024)
    private lateinit var scrollView: ScrollView
    private lateinit var logoImage: ImageView
    private lateinit var productsContainer: LinearLayout
    private lateinit var etCompanyName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etTel: EditText
    private lateinit var etAddress: EditText
    private lateinit var etBillTo: EditText
    private lateinit var tvTotal: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnDownload: Button
    private lateinit var btnSendToAdmin: Button
    private lateinit var btnViewQuotation: Button

    // Firebase database reference (Google Firebase, 2024)
    private lateinit var database: DatabaseReference
    private var quotationId: String? = null
    private var currentQuotation: Quotation? = null

    override fun onCreate(savedInstanceState: Bundle?) { // Lifecycle method (Android Developers, 2024)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quotation_view)

        initializeViews() // Initialize all UI views
        setupDatabase() // Connect to Firebase database
        loadIntentData() // Load quotation data passed via intent
        setupClickListeners() // Set up user interaction events
    }

    private fun initializeViews() { // Maps XML elements to Kotlin variables (Android Developers, 2024)
        scrollView = findViewById(R.id.scrollView)
        logoImage = findViewById(R.id.logoImage)
        productsContainer = findViewById(R.id.productsContainer)
        etCompanyName = findViewById(R.id.etCompanyName)
        etEmail = findViewById(R.id.etEmail)
        etTel = findViewById(R.id.etTel)
        etAddress = findViewById(R.id.etAddress)
        etBillTo = findViewById(R.id.etBillTo)
        tvTotal = findViewById(R.id.tvTotal)
        tvStatus = findViewById(R.id.tvStatus)
        btnDownload = findViewById(R.id.btnDownload)
        btnSendToAdmin = findViewById(R.id.btnSendToAdmin)
        btnViewQuotation = findViewById(R.id.btnViewQuotation)
    }

    private fun setupDatabase() { // Setup Firebase database reference (Google Firebase, 2024)
        database = FirebaseDatabase.getInstance().getReference("quotations")
    }

    private fun loadIntentData() { // Retrieve quotation data from intent (Android Developers, 2024)
        quotationId = intent.getStringExtra("quotationId")
        currentQuotation = intent.getParcelableExtra("quotation")

        val currentUser = FirebaseAuth.getInstance().currentUser // Get current user (Firebase Authentication, 2024)
        if (currentUser == null) { // Handle unauthenticated access
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load quotation data based on the provided identifier
        when {
            quotationId != null -> loadQuotationFromFirebase(quotationId!!, currentUser.uid)
            currentQuotation != null -> {
                if (currentQuotation!!.userId == currentUser.uid) {
                    displayQuotation(currentQuotation!!)
                } else {
                    Toast.makeText(this, "Unauthorized access", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            else -> Toast.makeText(this, "No quotation data provided", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() { // Handle button click events (Kotlinlang.org, 2024)
        btnDownload.setOnClickListener {
            currentQuotation?.let { downloadQuotation(it) }
        }

        btnSendToAdmin.setOnClickListener {
            currentQuotation?.let { openMessagingPage(it) }
        }

        btnViewQuotation.setOnClickListener {
            currentQuotation?.let { viewQuotation(it) }
        }
    }

    private fun loadQuotationFromFirebase(id: String, userId: String) { // Load quotation from database (Firebase Realtime Database, 2024)
        database.child(id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) { // Handle data retrieval success
                    if (!snapshot.exists()) {
                        Toast.makeText(
                            this@UserQuotationViewActivity,
                            "Quotation not found",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    val quotation = snapshot.getValue(Quotation::class.java) // Deserialize data (Google Firebase, 2024)
                    if (quotation == null) {
                        Toast.makeText(
                            this@UserQuotationViewActivity,
                            "Quotation data invalid",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    quotation.id = snapshot.key ?: ""
                    if (quotation.userId == userId) {
                        currentQuotation = quotation
                        displayQuotation(quotation)
                    } else {
                        Toast.makeText(
                            this@UserQuotationViewActivity,
                            "Unauthorized access",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) { // Handle data read failure
                    Toast.makeText(
                        this@UserQuotationViewActivity,
                        "Error loading quotation: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun displayQuotation(quotation: Quotation) { // Populate UI with quotation details (Android Developers, 2024)
        etCompanyName.setText(quotation.companyName)
        etEmail.setText(quotation.email)
        etTel.setText(quotation.phone)
        etAddress.setText(quotation.address)
        etBillTo.setText(quotation.billTo)

        tvTotal.text = "Total: R${String.format("%.2f", quotation.subtotal)}"
        tvStatus.text = "Status: ${quotation.status}"
        updateStatusColor(quotation.status) // Change color according to status
        displayProducts(quotation.items)
    }

    private fun displayProducts(items: List<QuotationItem>?) { // Display product list dynamically (Android Developers, 2024)
        productsContainer.removeAllViews()

        if (items.isNullOrEmpty()) {
            val emptyView = TextView(this).apply {
                text = "No products in this quotation"
                setPadding(16, 16, 16, 16)
            }
            productsContainer.addView(emptyView)
            return
        }

        for (item in items) {
            val itemView = LayoutInflater.from(this)
                .inflate(R.layout.quotation_product_display_item, productsContainer, false)
            itemView.findViewById<TextView>(R.id.txtProductName).text = item.name
            itemView.findViewById<TextView>(R.id.txtQuantity).text = "${item.quantity} units"
            itemView.findViewById<TextView>(R.id.txtPrice).text =
                "R${String.format("%.2f", item.total)}"
            productsContainer.addView(itemView)
        }
    }

    private fun updateStatusColor(status: String) { // Update status text color (Android Developers, 2024)
        tvStatus.setTextColor(
            when (status.lowercase(Locale.ROOT)) {
                "pending" -> getColor(android.R.color.holo_orange_dark)
                "approved" -> getColor(android.R.color.holo_green_dark)
                "rejected" -> getColor(android.R.color.holo_red_dark)
                else -> getColor(android.R.color.darker_gray)
            }
        )
    }

    private fun getPdfUri(pdfFile: File): Uri? { // Generate secure URI using FileProvider (Android Developers, 2024)
        return try {
            FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                pdfFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error generating file URI", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun downloadQuotation(quotation: Quotation) { // Generate and save PDF (GeeksforGeeks, 2024)
        val pdfFile = PdfGenerator.generateQuotationPdf(this, quotation)
        if (!pdfFile.exists()) {
            Toast.makeText(this, "PDF generation failed", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(this, "PDF saved at: ${pdfFile.absolutePath}", Toast.LENGTH_LONG).show()
    }

    private fun openMessagingPage(quotation: Quotation) { // Send quotation to admin chat (Google Firebase, 2024)
        val pdfFile = PdfGenerator.generateQuotationPdf(this, quotation)
        if (!pdfFile.exists()) {
            Toast.makeText(this, "PDF generation failed", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = getPdfUri(pdfFile) ?: return

        val intent = Intent(this, AdminChatViewActivity::class.java).apply {
            putExtra("chatId", "admin_chat_${FirebaseAuth.getInstance().currentUser?.uid}")
            putExtra("userEmail", quotation.email)
            putExtra("userId", FirebaseAuth.getInstance().currentUser?.uid)
            putExtra("quotationPdfUri", uri.toString())
        }
        startActivity(intent)
    }

    private fun viewQuotation(quotation: Quotation) { // Open PDF file in viewer (Android Developers, 2024)
        val pdfFile = PdfGenerator.generateQuotationPdf(this, quotation)
        if (!pdfFile.exists()) {
            Toast.makeText(this, "PDF generation failed", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = getPdfUri(pdfFile) ?: return

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            startActivity(Intent.createChooser(intent, "Open Quotation"))
        } catch (e: Exception) {
            Toast.makeText(this, "No app found to open PDF", Toast.LENGTH_SHORT).show()
        }
    }
}

/*
Reference List

Android Developers, 2024. Activity Lifecycle and UI Components. [online] Available at: <https://developer.android.com/guide/components/activities> [Accessed 25 September 2025].

Android Developers, 2024. FileProvider | Android Developers Documentation. [online] Available at: <https://developer.android.com/reference/androidx/core/content/FileProvider> [Accessed 21 October 2025].

Google Firebase, 2024. Firebase Realtime Database Guide. [online] Available at: <https://firebase.google.com/docs/database/android/start> [Accessed 29 August 2025].

Google Firebase, 2024. Firebase Authentication Overview. [online] Available at: <https://firebase.google.com/docs/auth/android/start> [Accessed 25 September 2025].

Kotlinlang.org, 2024. Kotlin Android Development Guide. [online] Available at: <https://kotlinlang.org/docs/android-overview.html> [Accessed 24 September 2025].

GeeksforGeeks, 2024. Generate PDF in Android Using iText or PdfDocument API. [online] Available at: <https://www.geeksforgeeks.org/generate-pdf-file-in-android-using-itextpdf/> [Accessed 24 September 2025].

Android Developers, 2024. Intents and Intent Filters in Android. [online] Available at: <https://developer.android.com/guide/components/intents-filters> [Accessed 24 September 2025].

*/
