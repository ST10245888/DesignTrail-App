package vcmsa.projects.fkj_consultants.activities

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import vcmsa.projects.fkj_consultants.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PromotionalMaterialsActivity : AppCompatActivity() {

    private lateinit var searchView: SearchView
    private lateinit var listView: ListView
    private lateinit var btnAddToCart: Button
    private lateinit var adapter: PromotionalAdapter

    private val selectedItems = mutableListOf<String>()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val services = listOf(
        PromotionalItem("Clothing", 150.0, R.drawable.ic_clothing),
        PromotionalItem("Kitchen Ware", 80.0, R.drawable.ic_kitchen),
        PromotionalItem("Bags", 120.0, R.drawable.ic_bag),
        PromotionalItem("Banners", 200.0, R.drawable.ic_banners),
        PromotionalItem("Caps", 90.0, R.drawable.ic_cap),
        PromotionalItem("Stationery", 50.0, R.drawable.ic_stationary),
        PromotionalItem("Posters", 30.0, R.drawable.ic_posters),
        PromotionalItem("Mugs", 70.0, R.drawable.ic_mugs),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_promotional_materials)

        searchView = findViewById(R.id.searchView)
        listView = findViewById(R.id.listViewServices)
        btnAddToCart = findViewById(R.id.btnAddToCart)

        adapter = PromotionalAdapter(this, services)
        listView.adapter = adapter

        // Search filter
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })

        // Item click -> popup dialog
        listView.setOnItemClickListener { _, _, position, _ ->
            val item = services[position]
            showQuantityDialog(item)
        }

        // Show details dialog before saving quotation
        btnAddToCart.setOnClickListener {
            if (selectedItems.isEmpty()) {
                Toast.makeText(this, "No items selected", Toast.LENGTH_SHORT).show()
            } else {
                showQuotationDetailsDialog()
            }
        }
    }

    private fun showQuantityDialog(item: PromotionalItem) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_quantity, null)
        val edtQuantity = dialogView.findViewById<EditText>(R.id.edtQuantity)

        AlertDialog.Builder(this)
            .setTitle("Add ${item.name}")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val quantity = edtQuantity.text.toString().toIntOrNull() ?: 0
                if (quantity > 0) {
                    val total = item.pricePerUnit * quantity
                    selectedItems.add("${item.name} x $quantity = R$total")
                    Toast.makeText(this, "${item.name} added", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showQuotationDetailsDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_quotation_details, null)

        val edtCompany = dialogView.findViewById<EditText>(R.id.edtCompanyName)
        val edtAddress = dialogView.findViewById<EditText>(R.id.edtAddress)
        val edtEmail = dialogView.findViewById<EditText>(R.id.edtEmail)
        val edtPhone = dialogView.findViewById<EditText>(R.id.edtPhone)
        val edtBillTo = dialogView.findViewById<EditText>(R.id.edtBillTo)

        AlertDialog.Builder(this)
            .setTitle("Quotation Details")
            .setView(dialogView)
            .setPositiveButton("Generate") { _, _ ->
                val companyName = edtCompany.text.toString()
                val address = edtAddress.text.toString()
                val email = edtEmail.text.toString()
                val phone = edtPhone.text.toString()
                val billTo = edtBillTo.text.toString()

                generateQuotationText(companyName, address, email, phone, billTo)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun generateQuotationText(
        companyName: String,
        address: String,
        email: String,
        phone: String,
        billTo: String
    ) {
        try {
            // Create "quotations" directory
            val dir = File(getExternalFilesDir(null), "quotations")
            if (!dir.exists()) dir.mkdirs()

            // Unique file name
            val fileName = "quotation_${System.currentTimeMillis()}.txt"
            val file = File(dir, fileName)

            val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

            // Calculate subtotal
            val subtotal = selectedItems.sumOf {
                val match = Regex("= R(\\d+(\\.\\d+)?)").find(it)
                match?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
            }

            // Write quotation data locally
            file.bufferedWriter().use { out ->
                out.write("QUOTATION\n")
                out.write("==========================\n")
                out.write("Company: $companyName\n")
                out.write("Address: $address\n")
                out.write("Email: $email\n")
                out.write("Phone: $phone\n")
                out.write("Bill To: $billTo\n")
                out.write("Date: $date\n")
                out.write("==========================\n\n")

                selectedItems.forEach { out.write("$it\n") }

                out.write("\n==========================\n")
                out.write("Subtotal: R$subtotal\n")
            }

            // ✅ Save quotation metadata to Firestore
            val userId = auth.currentUser?.uid ?: "unknown_user"
            val quotationData = hashMapOf(
                "userId" to userId,
                "companyName" to companyName,
                "address" to address,
                "email" to email,
                "phone" to phone,
                "billTo" to billTo,
                "fileName" to fileName,
                "filePath" to file.absolutePath,
                "subtotal" to subtotal,
                "timestamp" to System.currentTimeMillis(),
                "status" to "Pending"
            )


            firestore.collection("quotations")
                .add(quotationData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Quotation saved to Firebase", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving to Firebase: ${e.message}", Toast.LENGTH_LONG).show()
                }

            // Show dialog with option to open local file
            AlertDialog.Builder(this)
                .setTitle("Quotation Saved")
                .setMessage("File saved at:\n${file.absolutePath}")
                .setPositiveButton("Open") { _, _ ->
                    val uri = Uri.fromFile(file)
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "text/plain")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(intent, "Open Quotation"))
                }
                .setNegativeButton("Close", null)
                .show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving quotation: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
