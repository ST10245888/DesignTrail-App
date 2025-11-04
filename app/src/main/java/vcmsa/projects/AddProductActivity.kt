package vcmsa.projects.fkj_consultants.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.Product
import java.util.*

class AddProductActivity : AppCompatActivity() {

    private lateinit var ivImagePreview: ImageView
    private lateinit var btnPickImage: Button
    private lateinit var etProductName: EditText
    private lateinit var etProductDescription: EditText
    private lateinit var etProductColor: EditText
    private lateinit var etProductSize: EditText
    private lateinit var etProductPrice: EditText
    private lateinit var etProductQuantity: EditText
    private lateinit var etProductCategory: EditText
    private lateinit var spinnerAvailability: Spinner
    private lateinit var btnSaveProduct: Button
    private lateinit var btnCancel: Button

    private var pickedImageUri: Uri? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1001
        private const val TAG = "AddProductActivity"
    }

    private val storage = FirebaseStorage.getInstance().reference
    private val dbRef = FirebaseDatabase.getInstance().getReference("inventory")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        initializeViews()
        setupSpinner()
        setupClickListeners()
    }

    private fun initializeViews() {
        ivImagePreview = findViewById(R.id.ivImagePreview)
        btnPickImage = findViewById(R.id.btnPickImage)
        etProductName = findViewById(R.id.etProductName)
        etProductDescription = findViewById(R.id.etProductDescription)
        etProductColor = findViewById(R.id.etProductColor)
        etProductSize = findViewById(R.id.etProductSize)
        etProductPrice = findViewById(R.id.etProductPrice)
        etProductQuantity = findViewById(R.id.etProductQuantity)
        etProductCategory = findViewById(R.id.etProductCategory)
        spinnerAvailability = findViewById(R.id.spinnerAvailability)
        btnSaveProduct = findViewById(R.id.btnUpdateProduct)
        btnCancel = findViewById(R.id.btnCancel)
    }

    private fun setupSpinner() {
        val availabilityOptions = listOf("In Stock", "Out of Stock")
        spinnerAvailability.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            availabilityOptions
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun setupClickListeners() {
        btnPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        btnSaveProduct.setOnClickListener {
            if (validateInputs()) {
                saveProduct()
            }
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun validateInputs(): Boolean {
        val name = etProductName.text.toString().trim()
        val price = etProductPrice.text.toString().trim()
        val quantity = etProductQuantity.text.toString().trim()

        //(Nielsen, 2020)
        when {
            name.isEmpty() -> {
                etProductName.error = "Name is required"
                etProductName.requestFocus()
                return false
            }
            price.isEmpty() -> {
                etProductPrice.error = "Price is required"
                etProductPrice.requestFocus()
                return false
            }
            price.toDoubleOrNull() == null || price.toDouble() < 0 -> {
                etProductPrice.error = "Enter a valid price"
                etProductPrice.requestFocus()
                return false
            }
            quantity.isEmpty() -> {
                etProductQuantity.error = "Quantity is required"
                etProductQuantity.requestFocus()
                return false
            }
            quantity.toIntOrNull() == null || quantity.toInt() < 0 -> {
                etProductQuantity.error = "Enter a valid quantity"
                etProductQuantity.requestFocus()
                return false
            }
        }
        return true
    }

    private fun saveProduct() {
        val name = etProductName.text.toString().trim()
        val description = etProductDescription.text.toString().trim()
        val color = etProductColor.text.toString().trim()
        val size = etProductSize.text.toString().trim()
        val price = etProductPrice.text.toString().trim().toDouble()
        val quantity = etProductQuantity.text.toString().trim().toInt()
        val category = etProductCategory.text.toString().trim()
        val availability = spinnerAvailability.selectedItem.toString()

        val progress = ProgressDialog(this).apply {
            setMessage("Uploading product...")
            setCancelable(false)
            show()
        }

        // (Google Developers, 2024)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val imageUrl = pickedImageUri?.let { uploadImageToFirebase(it) } ?: ""

                val newRef = dbRef.push()
                val product = Product(
                    productId = newRef.key ?: "",
                    name = name,
                    description = description,
                    color = color,
                    size = size,
                    price = price,
                    quantity = quantity,
                    category = category,
                    availability = availability,
                    imageUrl = imageUrl,
                    timestamp = System.currentTimeMillis()
                )

                val productMap = mapOf(
                    "productId" to product.productId,
                    "name" to product.name,
                    "description" to product.description,
                    "color" to product.color,
                    "size" to product.size,
                    "price" to product.price,
                    "quantity" to product.quantity,
                    "category" to product.category,
                    "availability" to product.availability,
                    "imageUrl" to product.imageUrl,
                    "timestamp" to product.timestamp
                )

                // (JetBrains, 2023)
                newRef.setValue(productMap).await()

                withContext(Dispatchers.Main) {
                    progress.dismiss()

                    val resultIntent = Intent().apply {
                        putExtra("productId", product.productId)
                        putExtra("name", product.name)
                        putExtra("description", product.description)
                        putExtra("color", product.color)
                        putExtra("size", product.size)
                        putExtra("price", product.price)
                        putExtra("quantity", product.quantity)
                        putExtra("category", product.category)
                        putExtra("availability", product.availability)
                        putExtra("imageUrl", product.imageUrl)
                        putExtra("timestamp", product.timestamp)
                    }

                    setResult(Activity.RESULT_OK, resultIntent)
                    Toast.makeText(this@AddProductActivity, "Product added successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save product", e)
                withContext(Dispatchers.Main) {
                    progress.dismiss()
                    Toast.makeText(
                        this@AddProductActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private suspend fun uploadImageToFirebase(uri: Uri): String {
        return try {
            val fileRef = storage.child("product_images/${UUID.randomUUID()}.jpg")
            fileRef.putFile(uri).await()
            fileRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload image", e)
            throw e
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            pickedImageUri = data?.data
            pickedImageUri?.let {
                Glide.with(this)
                    .load(it)
                    .placeholder(R.drawable.ic_add)
                    .error(R.drawable.ic_add)
                    .into(ivImagePreview)
            }
        }
    }
}

/**
 * References
 *
 * Google Developers .2024. Cloud Firestore documentation. Available at: https://firebase.google.com/docs/firestore (Accessed: 4 November 2025).
 *
 * JetBrains. 2023. Kotlin coroutines overview. Available at: https://kotlinlang.org/docs/coroutines-overview.html (Accessed: 4 November 2025).
 *
 * Nielsen, J. 2020 10 Usability Heuristics for User Interface Design. Nielsen Norman Group. Available at: https://www.nngroup.com/articles/ten-usability-heuristics/ (Accessed: 4 November 2025).
 */
