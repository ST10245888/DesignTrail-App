package vcmsa.projects.fkj_consultants.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import vcmsa.projects.fkj_consultants.R

class AddProductActivity : AppCompatActivity() {

    private lateinit var ivImagePreview: ImageView
    private lateinit var btnPickImage: Button
    private lateinit var etProductImageUrl: EditText
    private lateinit var etProductName: EditText
    private lateinit var etProductColor: EditText
    private lateinit var etProductSize: EditText
    private lateinit var etProductPrice: EditText
    private lateinit var etProductCategory: EditText
    private lateinit var spinnerAvailability: Spinner
    private lateinit var btnSaveProduct: Button

    private var pickedImageUri: Uri? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        ivImagePreview = findViewById(R.id.ivImagePreview)
        btnPickImage = findViewById(R.id.btnPickImage)
        etProductImageUrl = findViewById(R.id.etProductImageUrl)
        etProductName = findViewById(R.id.etProductName)
        etProductColor = findViewById(R.id.etProductColor)
        etProductSize = findViewById(R.id.etProductSize)
        etProductPrice = findViewById(R.id.etProductPrice)
        etProductCategory = findViewById(R.id.etProductCategory)
        spinnerAvailability = findViewById(R.id.spinnerAvailability)
        btnSaveProduct = findViewById(R.id.btnSaveProduct)

        // Setup spinner
        val availabilityOptions = listOf("In Stock", "Out of Stock")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, availabilityOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAvailability.adapter = adapter

        // Pick image from gallery
        btnPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Save product
        btnSaveProduct.setOnClickListener {
            val name = etProductName.text.toString().trim()
            val description = "Color: ${etProductColor.text}, Size: ${etProductSize.text}"
            val quantity = 1 // default for now
            val imageUrl = pickedImageUri?.toString() ?: etProductImageUrl.text.toString().trim()

            if (name.isEmpty() || imageUrl.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Pass back to InventoryActivity
            val resultIntent = Intent()
            resultIntent.putExtra("name", name)
            resultIntent.putExtra("description", description)
            resultIntent.putExtra("quantity", quantity)
            resultIntent.putExtra("imageUrl", imageUrl)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
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
                    .into(ivImagePreview)
            }
        }
    }
}
