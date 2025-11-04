package vcmsa.projects.customer

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.models.Product

class ProductCustomizationActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_customization)

        // (Google, 2024)
        val productId = intent.getStringExtra("productId") ?: return

        val tvName = findViewById<TextView>(R.id.tvProductName)
        val etCustomization = findViewById<EditText>(R.id.etCustomization)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitOrder)

        // (Google Developers, 2024)
        db.collection("products").document(productId).get().addOnSuccessListener {
            val product = it.toObject(Product::class.java)
            tvName.text = product?.name
        }

        // (JetBrains, 2023)
        btnSubmit.setOnClickListener {
            val customText = etCustomization.text.toString()
            val customData = hashMapOf(
                "productId" to productId,
                "customization" to customText
            )

            db.collection("custom_orders").add(customData)
        }
    }
}

/**
 * References
 *
 * Google .2024. Intents and Intent Filters. Android Developers. Available at: https://developer.android.com/guide/components/intents-filters (Accessed: 4 November 2025).
 *
 * Google Developers .2024. Cloud Firestore documentation. Available at: https://firebase.google.com/docs/firestore (Accessed: 4 November 2025).
 *
 * JetBrains .2023. Kotlin Android development documentation. Available at: https://kotlinlang.org/docs/android-overview.html (Accessed: 4 November 2025).
 */
