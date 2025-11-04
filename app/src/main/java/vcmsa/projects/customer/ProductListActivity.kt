package vcmsa.projects.customer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import vcmsa.projects.adpters.ProductAdapter
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.Product

class ProductListActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val productList = mutableListOf<Product>()
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewProducts)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // (Google, 2024)
        adapter = ProductAdapter(productList) { product ->
            val intent = Intent(this, ProductCustomizationActivity::class.java)
            intent.putExtra("productId", product.productId)
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        //  (Google Developers, 2024)
        db.collection("products").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                productList.clear()
                for (doc in snapshot.documents) {
                    //(JetBrains, 2023)
                    val product = doc.toObject(Product::class.java)
                    if (product != null) productList.add(product)
                }
                adapter.notifyDataSetChanged()
            }
        }
    }
}

/**
 * References
 *
 * Google .2024. RecyclerView Overview. Android Developers. Available at: https://developer.android.com/guide/topics/ui/layout/recyclerview (Accessed: 4 November 2025).
 *
 * Google Developers .2024. Cloud Firestore documentation. Available at: https://firebase.google.com/docs/firestore (Accessed: 4 November 2025).
 *
 * JetBrains .2023. Kotlin data classes and object serialization. Available at: https://kotlinlang.org/docs/data-classes.html (Accessed: 4 November 2025).
 */
