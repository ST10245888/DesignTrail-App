package vcmsa.projects.fkj_consultants.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.adapter.MaterialAdapter
import vcmsa.projects.fkj_consultants.models.MaterialItem
import vcmsa.projects.fkj_consultants.models.MaterialListItem
import com.google.firebase.auth.FirebaseAuth


class SelectMaterialActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var materialRecyclerView: RecyclerView
    private lateinit var materialAdapter: MaterialAdapter
    private lateinit var categorySpinner: Spinner
    private lateinit var sortFilterSpinner: Spinner
    private lateinit var searchView: SearchView
    private lateinit var proceedButton: Button
    private lateinit var btnLogout: ImageButton
    private lateinit var btnViewBasket: ImageButton

    private val databaseRef = FirebaseDatabase.getInstance().getReference("inventory")

    private val fullList = mutableListOf<MaterialItem>()
    private val filteredList = mutableListOf<MaterialListItem>()

    // Basket holding tuples of (MaterialItem, quantity, selected color, selected size)
    private val basket = mutableListOf<BasketItem>()

    private var currentSearchText: String = ""
    private var currentCategory: String = "All"
    private var currentSortOption: String = "Best Seller"

    data class BasketItem(
        val material: MaterialItem,
        val quantity: Int,
        val selectedColor: String?,
        val selectedSize: String?
    )

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_material)

        materialRecyclerView = findViewById(R.id.recyclerViewMaterials)
        categorySpinner = findViewById(R.id.spinnerCategoryFilter)
        sortFilterSpinner = findViewById(R.id.spinnerSortFilter)
        searchView = findViewById(R.id.searchViewProduct)
        proceedButton = findViewById(R.id.btnContinue)
        btnLogout = findViewById(R.id.btnLogout)
        btnViewBasket = findViewById(R.id.btnViewBasket)

        auth = FirebaseAuth.getInstance()


        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnViewBasket.setOnClickListener {
            // Show a Toast with current basket item count
            Toast.makeText(this, "Basket has ${basket.size} items", Toast.LENGTH_SHORT).show()

            // Create an Intent to navigate to the BasketActivity
            val intent = Intent(this, BasketActivity::class.java)

            startActivity(intent)
        }


        materialRecyclerView.layoutManager = LinearLayoutManager(this)

        materialAdapter = MaterialAdapter(filteredList, this) { materialId, quantity, color, size ->
            val selectedMaterial = fullList.find { it.id == materialId }
            if (selectedMaterial != null) {
                addToBasket(selectedMaterial, quantity, color, size)
            } else {
                Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show()
            }
        }

        materialRecyclerView.adapter = materialAdapter

        setupSearchView()
        setupSortFilterSpinner()
        fetchInventoryFromRealtimeDatabase()

        proceedButton.setOnClickListener {
            Toast.makeText(this, "Basket contains ${basket.size} items", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to basket or checkout screen
        }
    }

    private fun addToBasket(material: MaterialItem, quantity: Int, color: String?, size: String?) {
        val existingItem = basket.find {
            it.material.id == material.id &&
                    it.selectedColor == color &&
                    it.selectedSize == size
        }
        if (existingItem != null) {
            val newQuantity = existingItem.quantity + quantity
            basket.remove(existingItem)
            basket.add(existingItem.copy(quantity = newQuantity))
        } else {
            basket.add(BasketItem(material, quantity, color, size))
        }
        Toast.makeText(this, "${material.name} added to basket", Toast.LENGTH_SHORT).show()
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchText = newText.orEmpty().trim()
                applyFilters()
                return true
            }
        })
    }

    private fun setupSortFilterSpinner() {
        val sortOptions = listOf(
            "Best Seller",
            "New",
            "Most Viewed",
            "Price: Low to High",
            "Price: High to Low"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sortOptions)
        sortFilterSpinner.adapter = adapter

        sortFilterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                currentSortOption = sortOptions[position]
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun fetchInventoryFromRealtimeDatabase() {
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fullList.clear()
                val categorySet = mutableSetOf("All")

                for (productSnapshot in snapshot.children) {
                    val id = productSnapshot.child("productId").getValue(String::class.java) ?: continue
                    val name = productSnapshot.child("name").getValue(String::class.java) ?: ""
                    val description = productSnapshot.child("description").getValue(String::class.java) ?: ""
                    val imageUrl = productSnapshot.child("imageUrl").getValue(String::class.java) ?: ""

                    val price = when (val priceValue = productSnapshot.child("price").value) {
                        is Double -> priceValue
                        is Long -> priceValue.toDouble()
                        is String -> priceValue.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }

                    val color = productSnapshot.child("color").getValue(String::class.java) ?: "N/A"
                    val size = productSnapshot.child("size").getValue(String::class.java) ?: "N/A"
                    val category = productSnapshot.child("category").getValue(String::class.java) ?: "Uncategorized"

                    categorySet.add(category)

                    val item = MaterialItem(
                        id = id,
                        name = name,
                        description = description,
                        imageUrl = imageUrl,
                        price = price,
                        availableColors = listOf(color),
                        availableSizes = listOf(size),
                        category = category
                    )

                    fullList.add(item)
                }

                setupCategorySpinner(categorySet.toList().sorted())
                applyFilters()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching inventory: ${error.message}", error.toException())
                Toast.makeText(this@SelectMaterialActivity, "Failed to load products", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupCategorySpinner(categories: List<String>) {
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = spinnerAdapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                currentCategory = categories[position]
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun applyFilters() {
        // Filter by category and search text
        val filteredMaterials = fullList.filter {
            val matchesCategory = currentCategory == "All" || it.category.equals(currentCategory, ignoreCase = true)
            val matchesSearch = it.name.contains(currentSearchText, ignoreCase = true)
            matchesCategory && matchesSearch
        }

        // Sort filteredMaterials by currentSortOption
        val sortedMaterials = when (currentSortOption) {
            "Price: Low to High" -> filteredMaterials.sortedBy { it.price }
            "Price: High to Low" -> filteredMaterials.sortedByDescending { it.price }
            // TODO: For demo, Best Seller, New, Most Viewed are not implemented, so default to original list
            else -> filteredMaterials
        }

        filteredList.clear()
        val grouped = sortedMaterials.groupBy { it.category }
        for ((category, items) in grouped) {
            filteredList.add(MaterialListItem.CategoryHeader(category))
            filteredList.addAll(items.map { MaterialListItem.MaterialEntry(it) })
        }

        materialAdapter.notifyDataSetChanged()
    }
}
