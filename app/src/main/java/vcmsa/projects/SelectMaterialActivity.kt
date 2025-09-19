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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.*
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.adapters.MaterialAdapter
import vcmsa.projects.fkj_consultants.models.BasketItem
import vcmsa.projects.fkj_consultants.models.MaterialListItem
import vcmsa.projects.fkj_consultants.models.Product

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

    private val fullList = mutableListOf<Product>()
    private val filteredList = mutableListOf<MaterialListItem>()
    private val basket = mutableListOf<BasketItem>()

    private var currentSearchText: String = ""
    private var currentCategory: String = "All"
    private var currentSortOption: String = "Best Seller"

    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_material)

        // Initialize Views
        materialRecyclerView = findViewById(R.id.recyclerViewMaterials)
        categorySpinner = findViewById(R.id.spinnerCategoryFilter)
        sortFilterSpinner = findViewById(R.id.spinnerSortFilter)
        searchView = findViewById(R.id.searchViewProduct)
        proceedButton = findViewById(R.id.btnContinue)
        btnLogout = findViewById(R.id.btnLogout)
        btnViewBasket = findViewById(R.id.btnViewBasket)

        auth = FirebaseAuth.getInstance()

        setupRecyclerView()
        setupSearchView()
        setupSortFilterSpinner()
        fetchInventoryFromFirebase()

        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnViewBasket.setOnClickListener {
            val intent = Intent(this, BasketActivity::class.java)
            intent.putParcelableArrayListExtra("basket", ArrayList(basket))
            startActivity(intent)
        }

        proceedButton.setOnClickListener {
            if (basket.isEmpty()) {
                Toast.makeText(this, "Your basket is empty!", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, QuotationGeneratorActivity::class.java)
                intent.putParcelableArrayListExtra("basket_items", ArrayList(basket))
                startActivity(intent)
            }
        }
    }

    private fun setupRecyclerView() {
        materialRecyclerView.layoutManager = LinearLayoutManager(this)
        materialAdapter = MaterialAdapter(filteredList, this) { product, quantity, color, size ->
            addToBasket(product, quantity, color, size)
        }
        materialRecyclerView.adapter = materialAdapter
    }

    private fun addToBasket(product: Product, quantity: Int, color: String?, size: String?) {
        val existingItem = basket.find {
            it.product.productId == product.productId &&
                    it.selectedColor == color &&
                    it.selectedSize == size
        }

        if (existingItem != null) {
            existingItem.quantity += quantity
        } else {
            basket.add(BasketItem(product, quantity, color, size))
        }
        Toast.makeText(this, "${product.name} added to basket", Toast.LENGTH_SHORT).show()
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchText = newText.orEmpty().trim()
                applyFiltersAsync()
                return true
            }
        })
    }

    private fun setupSortFilterSpinner() {
        val sortOptions = listOf("Best Seller", "New", "Most Viewed", "Price: Low to High", "Price: High to Low")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sortOptions)
        sortFilterSpinner.adapter = adapter
        sortFilterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                currentSortOption = sortOptions[position]
                applyFiltersAsync()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun fetchInventoryFromFirebase() {
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                ioScope.launch {
                    fullList.clear()
                    val categorySet = mutableSetOf("All")

                    snapshot.children.forEach { productSnapshot ->
                        val map = productSnapshot.value as? Map<*, *> ?: return@forEach
                        val product = Product(
                            productId = productSnapshot.key ?: "",
                            name = map["name"] as? String ?: "",
                            color = map["color"] as? String ?: "",
                            size = map["size"] as? String ?: "",
                            price = (map["price"] as? Number)?.toDouble()
                                ?: (map["price"] as? String)?.toDoubleOrNull()
                                ?: 0.0,
                            category = map["category"] as? String ?: "",
                            imageUrl = map["imageUrl"] as? String ?: "",
                            availability = map["availability"] as? String ?: "In Stock",
                            timestamp = (map["timestamp"] as? Number)?.toLong() ?: 0L
                        )

                        fullList.add(product)
                        if (product.category.isNotBlank()) categorySet.add(product.category)
                    }

                    withContext(Dispatchers.Main) {
                        setupCategorySpinner(categorySet.toList().sorted())
                        applyFiltersAsync()
                    }
                }
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
                applyFiltersAsync()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun applyFiltersAsync() {
        ioScope.launch {
            val filtered = fullList.filter {
                (currentCategory == "All" || it.category.equals(currentCategory, true)) &&
                        it.name.contains(currentSearchText, true)
            }

            val sorted = when (currentSortOption) {
                "Price: Low to High" -> filtered.sortedBy { it.price }
                "Price: High to Low" -> filtered.sortedByDescending { it.price }
                else -> filtered
            }

            val groupedList = mutableListOf<MaterialListItem>()
            sorted.groupBy { it.category }.forEach { (cat, products) ->
                groupedList.add(MaterialListItem.CategoryHeader(cat))
                groupedList.addAll(products.map { MaterialListItem.MaterialEntry(it) })
            }

            withContext(Dispatchers.Main) {
                filteredList.clear()
                filteredList.addAll(groupedList)
                materialAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ioScope.cancel()
    }
}
