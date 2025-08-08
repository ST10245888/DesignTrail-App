package vcmsa.projects.fkj_consultants.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import vcmsa.projects.fkj_consultants.R
import java.util.*

class InventoryActivity : AppCompatActivity() {

    private lateinit var etProductName: EditText
    private lateinit var etProductColor: EditText
    private lateinit var etProductSize: EditText
    private lateinit var etProductPrice: EditText
    private lateinit var etProductCategory: EditText
    private lateinit var etProductImageUrl: EditText
    private lateinit var spinnerAvailability: Spinner
    private lateinit var btnSaveProduct: Button

    private val availabilityOptions = listOf("In Stock", "Out of Stock")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory)

        etProductName = findViewById(R.id.etProductName)
        etProductColor = findViewById(R.id.etProductColor)
        etProductSize = findViewById(R.id.etProductSize)
        etProductPrice = findViewById(R.id.etProductPrice)
        etProductCategory = findViewById(R.id.etProductCategory)
        etProductImageUrl = findViewById(R.id.etProductImageUrl)
        spinnerAvailability = findViewById(R.id.spinnerAvailability)
        btnSaveProduct = findViewById(R.id.btnSaveProduct)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, availabilityOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAvailability.adapter = adapter

        btnSaveProduct.setOnClickListener {
            saveProduct()
        }
    }

    private fun saveProduct() {
        val name = etProductName.text.toString().trim()
        val color = etProductColor.text.toString().trim()
        val size = etProductSize.text.toString().trim()
        val priceText = etProductPrice.text.toString().trim()
        val category = etProductCategory.text.toString().trim()
        val imageUrl = etProductImageUrl.text.toString().trim()
        val availability = spinnerAvailability.selectedItem.toString()

        if (name.isEmpty() || color.isEmpty() || size.isEmpty() || priceText.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceText.toDoubleOrNull()
        if (price == null) {
            Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show()
            return
        }

        val productId = UUID.randomUUID().toString()
        val product = mapOf(
            "productId" to productId,
            "name" to name,
            "color" to color,
            "size" to size,
            "price" to price,
            "category" to category,
            "imageUrl" to imageUrl,
            "availability" to availability,
            "timestamp" to System.currentTimeMillis()
        )

        FirebaseDatabase.getInstance().getReference("inventory")
            .child(productId)
            .setValue(product)
            .addOnSuccessListener {
                Toast.makeText(this, "Product saved", Toast.LENGTH_SHORT).show()
                clearFields()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save product", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearFields() {
        etProductName.text.clear()
        etProductColor.text.clear()
        etProductSize.text.clear()
        etProductPrice.text.clear()
        etProductCategory.text.clear()
        etProductImageUrl.text.clear()
        spinnerAvailability.setSelection(0)
    }
}
