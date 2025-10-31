package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.Product

class InventoryActivity : AppCompatActivity() {

    private lateinit var recyclerInventory: RecyclerView
    private lateinit var btnAddProduct: Button
    private lateinit var connectionBanner: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateText: TextView

    private val inventoryList = mutableListOf<Product>()
    private lateinit var adapter: InventoryAdapter
    private lateinit var dbRef: DatabaseReference
    private lateinit var connectedRef: DatabaseReference

    private var lastTimestamp: Long? = null
    private val pageSize = 20
    private var isLoading = false

    companion object {
        private const val TAG = "InventoryActivity"
    }

    private val addProductLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                try {
                    val newItem = Product(
                        productId = data.getStringExtra("productId") ?: "",
                        name = data.getStringExtra("name") ?: return@registerForActivityResult,
                        description = data.getStringExtra("description") ?: "",
                        quantity = data.getIntExtra("quantity", 0),
                        color = data.getStringExtra("color") ?: "",
                        size = data.getStringExtra("size") ?: "",
                        price = data.getDoubleExtra("price", 0.0),
                        category = data.getStringExtra("category") ?: "",
                        imageUrl = data.getStringExtra("imageUrl") ?: "",
                        availability = data.getStringExtra("availability") ?: "In Stock",
                        timestamp = data.getLongExtra("timestamp", System.currentTimeMillis())
                    )

                    inventoryList.add(0, newItem) // Add to top
                    adapter.submitList(inventoryList.toList())
                    recyclerInventory.scrollToPosition(0)
                    updateEmptyState()
                    Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e(TAG, "Error adding product from result", e)
                    Toast.makeText(this, "Error adding product", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_list)

        initializeViews()
        setupFirebase()
        setupConnectionBanner()
        setupRecyclerView()
        setupClickListeners()
        loadInventory()
        setupScrollPagination()
    }

    private fun initializeViews() {
        recyclerInventory = findViewById(R.id.recyclerInventory)
        btnAddProduct = findViewById(R.id.btnAddProduct)
        connectionBanner = findViewById(R.id.connectionStatusBanner)
        progressBar = findViewById(R.id.progressBar)
        emptyStateText = findViewById(R.id.emptyStateText)
    }

    private fun setupFirebase() {
        dbRef = FirebaseDatabase.getInstance().getReference("inventory")
        dbRef.keepSynced(true)
        connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected")
    }

    private fun setupClickListeners() {
        btnAddProduct.setOnClickListener {
            val intent = Intent(this, AddProductActivity::class.java)
            addProductLauncher.launch(intent)
        }
    }

    private fun setupRecyclerView() {
        adapter = InventoryAdapter(object : InventoryAdapter.InventoryListener {
            override fun onEdit(product: Product) {
                val intent = Intent(this@InventoryActivity, EditProductActivity::class.java).apply {
                    putExtra("productId", product.productId)
                    putExtra("name", product.name)
                    putExtra("description", product.description)
                    putExtra("color", product.color)
                    putExtra("size", product.size)
                    putExtra("price", product.price)
                    putExtra("category", product.category)
                    putExtra("availability", product.availability)
                    putExtra("imageUrl", product.imageUrl)
                    putExtra("quantity", product.quantity)
                }
                startActivity(intent)
            }

            override fun onDelete(product: Product) {
                showDeleteConfirmation(product)
            }

            override fun onToggleAvailability(product: Product) {
                val newAvailability = if (product.availability == "In Stock") "Out of Stock" else "In Stock"
                dbRef.child(product.productId).updateChildren(mapOf("availability" to newAvailability))
                    .addOnSuccessListener {
                        product.availability = newAvailability
                        adapter.notifyItemChanged(inventoryList.indexOf(product))
                        Toast.makeText(this@InventoryActivity, "Status updated", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to update availability", e)
                        Toast.makeText(this@InventoryActivity, "Update failed", Toast.LENGTH_SHORT).show()
                    }
            }
        })
        recyclerInventory.layoutManager = LinearLayoutManager(this)
        recyclerInventory.adapter = adapter
    }

    private fun showDeleteConfirmation(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete ${product.name}?")
            .setPositiveButton("Delete") { _, _ ->
                deleteProduct(product)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteProduct(product: Product) {
        dbRef.child(product.productId).removeValue()
            .addOnSuccessListener {
                inventoryList.remove(product)
                adapter.submitList(inventoryList.toList())
                updateEmptyState()
                Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to delete product", e)
                Toast.makeText(this, "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadInventory() {
        if (isLoading) return
        isLoading = true
        showLoading(true)

        var query: Query = dbRef.orderByChild("timestamp").limitToLast(pageSize)
        lastTimestamp?.let { ts ->
            query = dbRef.orderByChild("timestamp").endBefore(ts.toDouble()).limitToLast(pageSize)
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempList = mutableListOf<Product>()
                for (item in snapshot.children) {
                    safeDeserialize(item)?.let { tempList.add(it) }
                }

                // Sort by timestamp descending (newest first)
                tempList.sortByDescending { it.timestamp }

                if (tempList.isNotEmpty()) {
                    lastTimestamp = tempList.last().timestamp
                    inventoryList.addAll(tempList)
                    adapter.submitList(inventoryList.toList())
                }

                showLoading(false)
                updateEmptyState()
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Load failed", error.toException())
                Toast.makeText(this@InventoryActivity, "Load failed: ${error.message}", Toast.LENGTH_SHORT).show()
                showLoading(false)
                isLoading = false
            }
        })
    }

    private fun setupScrollPagination() {
        val layoutManager = recyclerInventory.layoutManager as LinearLayoutManager
        recyclerInventory.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return // Only load when scrolling down

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                // Load more when reaching the bottom
                if (!isLoading && (lastVisibleItem + 3) >= totalItemCount && totalItemCount >= pageSize) {
                    loadInventory()
                }
            }
        })
    }

    private fun setupConnectionBanner() {
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                connectionBanner.visibility = View.VISIBLE
                if (connected) {
                    connectionBanner.text = "Online"
                    connectionBanner.setBackgroundColor(getColor(R.color.status_online))
                } else {
                    connectionBanner.text = "Offline - Using Cached Data"
                    connectionBanner.setBackgroundColor(getColor(R.color.status_offline))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Connection check cancelled", error.toException())
                connectionBanner.visibility = View.VISIBLE
                connectionBanner.text = "Offline - Using Cached Data"
                connectionBanner.setBackgroundColor(getColor(R.color.status_offline))
            }
        })
    }

    private fun safeDeserialize(item: DataSnapshot): Product? {
        return try {
            val name = item.child("name").getValue(String::class.java) ?: ""
            if (name.isEmpty()) {
                Log.w(TAG, "Skipping item with no name: ${item.key}")
                return null
            }

            val description = item.child("description").getValue(String::class.java) ?: ""
            val color = item.child("color").getValue(String::class.java) ?: ""
            val size = item.child("size").getValue(String::class.java) ?: ""
            val category = item.child("category").getValue(String::class.java) ?: ""
            val imageUrl = item.child("imageUrl").getValue(String::class.java) ?: ""
            val availability = item.child("availability").getValue(String::class.java) ?: "In Stock"

            // Safe numeric deserialization with multiple type handling
            val quantity = when (val qtyValue = item.child("quantity").value) {
                is Long -> qtyValue.toInt()
                is Double -> qtyValue.toInt()
                is Int -> qtyValue
                is String -> qtyValue.toIntOrNull() ?: 0
                else -> {
                    Log.w(TAG, "Unknown quantity type for ${item.key}: ${qtyValue?.javaClass?.simpleName}")
                    0
                }
            }

            val price = when (val priceValue = item.child("price").value) {
                is Double -> priceValue
                is Long -> priceValue.toDouble()
                is Int -> priceValue.toDouble()
                is Float -> priceValue.toDouble()
                is String -> {
                    priceValue.toDoubleOrNull() ?: run {
                        Log.w(TAG, "Invalid price string for ${item.key}: $priceValue")
                        0.0
                    }
                }
                else -> {
                    Log.w(TAG, "Unknown price type for ${item.key}: ${priceValue?.javaClass?.simpleName}")
                    0.0
                }
            }

            val timestamp = when (val tsValue = item.child("timestamp").value) {
                is Long -> tsValue
                is Double -> tsValue.toLong()
                is Int -> tsValue.toLong()
                is String -> tsValue.toLongOrNull() ?: System.currentTimeMillis()
                else -> {
                    Log.w(TAG, "Unknown timestamp type for ${item.key}: ${tsValue?.javaClass?.simpleName}")
                    System.currentTimeMillis()
                }
            }

            Product(
                productId = item.key ?: "",
                name = name,
                description = description,
                quantity = quantity,
                color = color,
                size = size,
                price = price,
                category = category,
                imageUrl = imageUrl,
                availability = availability,
                timestamp = timestamp
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to deserialize item: ${item.key}", e)
            Toast.makeText(this, "Skipped corrupted item: ${item.key}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun updateEmptyState() {
        emptyStateText.visibility = if (inventoryList.isEmpty()) View.VISIBLE else View.GONE
        recyclerInventory.visibility = if (inventoryList.isEmpty()) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to activity
        refreshInventory()
    }

    private fun refreshInventory() {
        inventoryList.clear()
        lastTimestamp = null
        isLoading = false
        loadInventory()
    }
}
