package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import vcmsa.projects.fkj_consultants.R

class QuotationActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: QuotationAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val quotations = mutableListOf<Quotation>()
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quotation)

        recyclerView = findViewById(R.id.recyclerQuotations)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = QuotationAdapter(quotations) { quotation ->
            val intent = Intent(this, QuotationViewerActivity::class.java)
            intent.putExtra("filePath", quotation.filePath)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        listenQuotations()
    }

    private fun listenQuotations() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Real-time listener
        listenerRegistration = firestore.collection("quotations")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Error loading quotations: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    quotations.clear()
                    for (doc in snapshots.documents) {
                        val quotation = doc.toObject(Quotation::class.java)
                        if (quotation != null) quotations.add(quotation)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove() // Stop listening when activity is destroyed
    }
}
