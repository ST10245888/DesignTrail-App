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
import java.util.*

class EditProductActivity : AppCompatActivity() {

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
    private lateinit var btnUpdateProduct: Button
    private lateinit var btnCancel: Button

    private var pickedImageUri: Uri? = null
    private var currentImageUrl: String = ""
    private lateinit var productId: String

    companion object {
        private const val PICK_IMAGE_REQUEST = 1002
        private const val TAG = "EditProductActivity"
    }

    private val storage = FirebaseStorage.getInstance().reference
    private val dbRef = FirebaseDatabase.getInstance().getReference("inventory")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_product)

        initializeViews()
        setupSpinner()
        loadProductData()
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
        btnUpdateProduct = findViewById(R.id.btnUpdateProduct)
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

    private fun loadProductData() {
        productId = intent.getStringExtra("productId") ?: run {
            Toast.makeText(this, "Product ID missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        etProductName.setText(intent.getStringExtra("name") ?: "")
        etProductDescription.setText(intent.getStringExtra("description") ?: "")
        etProductColor.setText(intent.getStringExtra("color") ?: "")
        etProductSize.setText(intent.getStringExtra("size") ?: "")
        etProductPrice.setText(intent.getDoubleExtra("price", 0.0).toString())
        etProductQuantity.setText(intent.getIntExtra("quantity", 0).toString())
        etProductCategory.setText(intent.getStringExtra("category") ?: "")

        val availability = intent.getStringExtra("availability") ?: "In Stock"
        spinnerAvailability.setSelection(if (availability == "In Stock") 0 else 1)

        currentImageUrl = intent.getStringExtra("imageUrl") ?: ""
        if (currentImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(currentImageUrl)
                .placeholder(R.drawable.ic_add)
                .error(R.drawable.ic_add)
                .into(ivImagePreview)
        }
    }

    private fun setupClickListeners() {
        btnPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        btnUpdateProduct.setOnClickListener {
            if (validateInputs()) {
                updateProduct()
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

    private fun updateProduct() {
        val name = etProductName.text.toString().trim()
        val description = etProductDescription.text.toString().trim()
        val color = etProductColor.text.toString().trim()
        val size = etProductSize.text.toString().trim()
        val price = etProductPrice.text.toString().trim().toDouble()
        val quantity = etProductQuantity.text.toString().trim().toInt()
        val category = etProductCategory.text.toString().trim()
        val availability = spinnerAvailability.selectedItem.toString()

        val progress = ProgressDialog(this).apply {
            setMessage("Updating product...")
            setCancelable(false)
            show()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Upload new image if selected, otherwise keep current
                val imageUrl = if (pickedImageUri != null) {
                    uploadImageToFirebase(pickedImageUri!!)
                } else {
                    currentImageUrl
                }

                // Update with proper types
                val updates = mapOf(
                    "name" to name,
                    "description" to description,
                    "color" to color,
                    "size" to size,
                    "price" to price, // Double
                    "quantity" to quantity, // Int
                    "category" to category,
                    "availability" to availability,
                    "imageUrl" to imageUrl
                )

                dbRef.child(productId).updateChildren(updates).await()

                withContext(Dispatchers.Main) {
                    progress.dismiss()
                    Toast.makeText(
                        this@EditProductActivity,
                        "Product updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update product", e)
                withContext(Dispatchers.Main) {
                    progress.dismiss()
                    Toast.makeText(
                        this@EditProductActivity,
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

