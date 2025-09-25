package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.Product

class InventoryActivity : AppCompatActivity() {

    private lateinit var recyclerInventory: RecyclerView
    private lateinit var btnAddProduct: Button
    private val inventoryList = mutableListOf<Product>()
    private lateinit var adapter: InventoryAdapter

    private val addProductLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            data?.let {
                val name = it.getStringExtra("name") ?: return@let
                val description = it.getStringExtra("description") ?: ""
                val quantity = it.getIntExtra("quantity", 0)
                val imageUrl = it.getStringExtra("imageUrl")

                val newItem = Product(
                    name = name,
                    description = description,
                    quantity = quantity,
                    imageUrl = imageUrl ?: ""
                )
                inventoryList.add(newItem)
                adapter.notifyItemInserted(inventoryList.size - 1)
                recyclerInventory.scrollToPosition(inventoryList.size - 1)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_list)

        recyclerInventory = findViewById(R.id.recyclerInventory)
        btnAddProduct = findViewById(R.id.btnAddProduct)

        // Sample inventory items
        inventoryList.add(Product(name = "T-Shirt", description = "Cotton T-Shirt", quantity = 152))
        inventoryList.add(Product(name = "Cap", description = "Embroidered Cap", quantity = 130))
        inventoryList.add(Product(name = "Mug", description = "Ceramic Mug", quantity = 74))

        adapter = InventoryAdapter(
            inventoryList,
            listener = object : InventoryAdapter.InventoryListener {
                override fun onEdit(product: Product) {
                    // handle edit
                }
                override fun onDelete(product: Product) {
                    // handle delete
                }
                override fun onToggleAvailability(product: Product) {
                    // handle toggle
                }
            }
        )

        recyclerInventory.layoutManager = LinearLayoutManager(this)
        recyclerInventory.adapter = adapter

        btnAddProduct.setOnClickListener {
            val intent = Intent(this, AddProductActivity::class.java)
            addProductLauncher.launch(intent)
        }
    }
}
