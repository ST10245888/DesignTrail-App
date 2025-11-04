package vcmsa.projects.fkj_consultants

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import vcmsa.projects.fkj_consultants.models.Product

class ProductManagementTest {

    private val db = FirebaseFirestore.getInstance()

    @Test
    fun testAddProductAndRetrieve() {
        runBlocking {
            // (Sommerville, 2016)
            val product = Product(
                productId = "test123",
                name = "Test Product",
                description = "For testing",
                price = 49.99,
            )

            //(Google Developers, 2024)
            db.collection("products").document(product.productId).set(product).await()

            // (JetBrains, 2023)
            val snapshot = db.collection("products").document("test123").get().await()
            val retrieved = snapshot.toObject(Product::class.java)


            assertNotNull(retrieved)
            assertEquals("Test Product", retrieved?.name)
            retrieved?.price?.let { assertEquals(49.99, it, 0.01) }
        }
    }
}

/**
 * References:
 * Google Developers. 2024. Cloud Firestore documentation. Retrieved from https://firebase.google.com/docs/firestore
 * JetBrains. 2023. Kotlin coroutines guide. Retrieved from https://kotlinlang.org/docs/coroutines-overview.html
 * Sommerville, I. 2016. Software Engineering (10th ed.). Pearson Education.
 */
