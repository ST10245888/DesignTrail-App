package vcmsa.projects.fkj_consultants.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.Product
import java.util.*

class InventoryActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etColor: EditText
    private lateinit var etSize: EditText
    private lateinit var etPrice: EditText
    private lateinit var etCategory: EditText
    private lateinit var etImageUrl: EditText
    private lateinit var spinnerAvailability: Spinner
    private lateinit var btnSave: Button
    private lateinit var btnPickImage: Button
    private lateinit var ivPreview: ImageView

    private val availabilityOptions = listOf("In Stock", "Out of Stock")
    private var pickedImageUri: Uri? = null
    private var editingProductId: String? = null

    private val dbRef = FirebaseDatabase.getInstance().getReference("inventory")
    private val storageRef = FirebaseStorage.getInstance().reference.child("inventory_images")

    companion object {
        private const val REQ_PICK_IMAGE = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory)

        etName = findViewById(R.id.etProductName)
        etColor = findViewById(R.id.etProductColor)
        etSize = findViewById(R.id.etProductSize)
        etPrice = findViewById(R.id.etProductPrice)
        etCategory = findViewById(R.id.etProductCategory)
        etImageUrl = findViewById(R.id.etProductImageUrl)
        spinnerAvailability = findViewById(R.id.spinnerAvailability)
        btnSave = findViewById(R.id.btnSaveProduct)
        btnPickImage = findViewById(R.id.btnPickImage)
        ivPreview = findViewById(R.id.ivImagePreview)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, availabilityOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAvailability.adapter = adapter

        // If started for edit, populate fields
        val editProduct = intent.getParcelableExtra<Product>("edit_product")
        editProduct?.let { populateForEdit(it) }

        btnPickImage.setOnClickListener { pickImageFromGallery() }
        btnSave.setOnClickListener { saveProduct() }
    }

    private fun populateForEdit(p: Product) {
        editingProductId = p.productId
        etName.setText(p.name)
        etColor.setText(p.color)
        etSize.setText(p.size)
        etPrice.setText(p.price.toString())
        etCategory.setText(p.category)
        etImageUrl.setText(p.imageUrl)
        val idx = availabilityOptions.indexOf(p.availability).coerceAtLeast(0)
        spinnerAvailability.setSelection(idx)
        // load preview if URL
        if (p.imageUrl.isNotBlank()) {
            etImageUrl.setText(p.imageUrl)
            // show preview using MediaStore (simple)
            try {
                val uri = Uri.parse(p.imageUrl)
                ivPreview.setImageURI(uri)
            } catch (_: Exception) {}
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQ_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            pickedImageUri = data?.data
            ivPreview.setImageURI(pickedImageUri)
            // set the image URL field to blank until upload
            etImageUrl.setText("")
        }
    }

    private fun saveProduct() {
        val name = etName.text.toString().trim()
        val color = etColor.text.toString().trim()
        val size = etSize.text.toString().trim()
        val priceStr = etPrice.text.toString().trim()
        val category = etCategory.text.toString().trim()
        val availability = spinnerAvailability.selectedItem.toString()

        if (name.isEmpty() || priceStr.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill required fields (name, price, category).", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull()
        if (price == null) {
            Toast.makeText(this, "Invalid price.", Toast.LENGTH_SHORT).show()
            return
        }

        val id = editingProductId ?: dbRef.push().key ?: UUID.randomUUID().toString()
        // if user picked an image, upload it first, otherwise use text field or empty
        if (pickedImageUri != null) {
            val imgRef = storageRef.child("$id-${System.currentTimeMillis()}")
            val uploadTask = imgRef.putFile(pickedImageUri!!)
            uploadTask.addOnSuccessListener {
                imgRef.downloadUrl.addOnSuccessListener { uri ->
                    writeProductToDb(id, name, color, size, price, category, uri.toString(), availability)
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to get uploaded image URL.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Image upload failed.", Toast.LENGTH_SHORT).show()
            }
        } else {
            val imageUrl = etImageUrl.text.toString().trim()
            writeProductToDb(id, name, color, size, price, category, imageUrl, availability)
        }
    }

    private fun writeProductToDb(
        id: String, name: String, color: String, size: String, price: Double,
        category: String, imageUrl: String, availability: String
    ) {
        val product = Product(
            productId = id,
            name = name,
            color = color,
            size = size,
            price = price,
            category = category,
            imageUrl = imageUrl,
            availability = availability,
            timestamp = System.currentTimeMillis()
        )
        dbRef.child(id).setValue(product)
            .addOnSuccessListener {
                Toast.makeText(this, "Product saved.", Toast.LENGTH_SHORT).show()
                clearFields()
                finish() // go back to list if desired
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save product.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearFields() {
        etName.text.clear()
        etColor.text.clear()
        etSize.text.clear()
        etPrice.text.clear()
        etCategory.text.clear()
        etImageUrl.text.clear()
        ivPreview.setImageResource(R.drawable.placeholder_image)
        spinnerAvailability.setSelection(0)
        pickedImageUri = null
        editingProductId = null
    }
}
