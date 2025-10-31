package vcmsa.projects.fkj_consultants.activities

import android.app.AlertDialog
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PromotionalMaterialsActivity : AppCompatActivity() {

    private lateinit var searchView: SearchView
    private lateinit var listView: ListView
    private lateinit var btnAddToCart: Button
    private lateinit var adapter: PromotionalAdapter
    private val selectedItems = mutableListOf<CartItem>()

    private lateinit var dbRef: DatabaseReference
    private val auth = FirebaseAuth.getInstance()
    private var inventoryItems = listOf<PromotionalItem>()

    data class CartItem(
        val name: String,
        val quantity: Int,
        val pricePerUnit: Double,
        val total: Double
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_promotional_materials)

        dbRef = FirebaseDatabase.getInstance().getReference("inventory")

        searchView = findViewById(R.id.searchView)
        listView = findViewById(R.id.listViewServices)
        btnAddToCart = findViewById(R.id.btnAddToCart)

        adapter = PromotionalAdapter(this, inventoryItems)
        listView.adapter = adapter

        fetchInventoryItems()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })

        listView.setOnItemClickListener { _, _, position, _ ->
            adapter.getItem(position)?.let { item ->
                if (item.availability == "In Stock") {
                    showQuantityDialog(item)
                } else {
                    Toast.makeText(this, "${item.name} is out of stock", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnAddToCart.setOnClickListener {
            if (selectedItems.isEmpty()) {
                Toast.makeText(this, "No items selected", Toast.LENGTH_SHORT).show()
            } else {
                showQuotationDetailsDialog()
            }
        }
    }

    private fun fetchInventoryItems() {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<PromotionalItem>()
                for (itemSnap in snapshot.children) {
                    val name = itemSnap.child("name").getValue(String::class.java) ?: continue
                    val priceValue = itemSnap.child("price").value
                    val price = when (priceValue) {
                        is Number -> priceValue.toDouble()
                        is String -> priceValue.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }
                    val imageUrl = itemSnap.child("imageUrl").getValue(String::class.java)
                    val availability = itemSnap.child("availability").getValue(String::class.java) ?: "Unknown"
                    val category = itemSnap.child("category").getValue(String::class.java) ?: "Uncategorized"
                    val size = itemSnap.child("size").getValue(String::class.java) ?: ""
                    val color = itemSnap.child("color").getValue(String::class.java) ?: ""
                    val productId = itemSnap.child("productId").getValue(String::class.java) ?: ""
                    val stability = itemSnap.child("stability").getValue(Int::class.java) ?: 0
                    val quantity = if (availability == "In Stock") 10 else 0
                    val iconRes = R.drawable.ic_placeholder

                    items.add(
                        PromotionalItem(
                            name = name,
                            pricePerUnit = price,
                            iconRes = iconRes,
                            imageUrl = imageUrl,
                            quantity = quantity,
                            category = category,
                            color = color,
                            size = size,
                            productId = productId,
                            availability = availability,
                            stability = stability
                        )
                    )
                }
                inventoryItems = items
                adapter.updateItems(items)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@PromotionalMaterialsActivity,
                    "Failed to load inventory: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun showQuantityDialog(item: PromotionalItem) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_quantity, null)
        val edtQuantity = dialogView.findViewById<EditText>(R.id.edtQuantity)

        AlertDialog.Builder(this)
            .setTitle("Add ${item.name}")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val quantity = edtQuantity.text.toString().toIntOrNull() ?: 0
                if (quantity > 0 && quantity <= item.quantity) {
                    val total = item.pricePerUnit * quantity
                    selectedItems.add(CartItem(item.name, quantity, item.pricePerUnit, total))
                    Toast.makeText(this, "${item.name} added to cart", Toast.LENGTH_SHORT).show()
                    btnAddToCart.text = "Generate Quotation (${selectedItems.size} items)"
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
        val productsContainer = dialogView.findViewById<LinearLayout>(R.id.productsContainer)

        // Display selected products in the container
        productsContainer.removeAllViews()
        selectedItems.forEach { item ->
            val tv = TextView(this).apply {
                text = "${item.name} - Qty: ${item.quantity}, R${String.format("%.2f", item.total)}"
                textSize = 14f
                setPadding(8, 4, 8, 4)
            }
            productsContainer.addView(tv)
        }

        AlertDialog.Builder(this)
            .setTitle("Quotation Details")
            .setView(dialogView)
            .setPositiveButton("Generate") { _, _ ->
                val company = edtCompany.text.toString().trim()
                val address = edtAddress.text.toString().trim()
                val email = edtEmail.text.toString().trim()
                val phone = edtPhone.text.toString().trim()
                val billTo = edtBillTo.text.toString().trim()

                if (company.isEmpty() || email.isEmpty()) {
                    Toast.makeText(this, "Company and Email are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                generateQuotationText(company, address, email, phone, billTo)
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
            val dir = File(getExternalFilesDir(null), "quotations")
            if (!dir.exists()) dir.mkdirs()

            val timestamp = System.currentTimeMillis()
            val fileName = "quotation_$timestamp.txt"
            val file = File(dir, fileName)

            val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            val subtotal = selectedItems.sumOf { it.total }

            file.bufferedWriter().use { out ->
                out.write("═══════════════════════════════════════\n")
                out.write("              QUOTATION\n")
                out.write("═══════════════════════════════════════\n\n")
                out.write("Company: $companyName\n")
                out.write("Address: $address\n")
                out.write("Email: $email\n")
                out.write("Phone: $phone\n")
                out.write("Bill To: $billTo\n")
                out.write("Date: $date\n\n")
                out.write("═══════════════════════════════════════\n")
                out.write("              ITEMS\n")
                out.write("═══════════════════════════════════════\n\n")
                selectedItems.forEach { item ->
                    out.write("${item.name}\n")
                    out.write("  Quantity: ${item.quantity}\n")
                    out.write("  Price per unit: R${String.format("%.2f", item.pricePerUnit)}\n")
                    out.write("  Total: R${String.format("%.2f", item.total)}\n\n")
                }
                out.write("═══════════════════════════════════════\n")
                out.write("Subtotal: R${String.format("%.2f", subtotal)}\n")
                out.write("═══════════════════════════════════════\n")
            }

            val userId = auth.currentUser?.uid ?: "unknown_user"
            val itemsList = selectedItems.map {
                mapOf(
                    "name" to it.name,
                    "quantity" to it.quantity,
                    "pricePerUnit" to it.pricePerUnit,
                    "total" to it.total
                )
            }

            val quotationData = mapOf(
                "userId" to userId,
                "companyName" to companyName,
                "address" to address,
                "email" to email,
                "phone" to phone,
                "billTo" to billTo,
                "fileName" to fileName,
                "filePath" to file.absolutePath,
                "items" to itemsList,
                "subtotal" to subtotal,
                "timestamp" to timestamp,
                "status" to "Pending",
                "type" to "promotional_materials"
            )

            // Save to Firebase Realtime Database
            val dbQuotationRef = FirebaseDatabase.getInstance().getReference("quotations").push()
            dbQuotationRef.setValue(quotationData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Quotation saved to database", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
                }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving quotation: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
