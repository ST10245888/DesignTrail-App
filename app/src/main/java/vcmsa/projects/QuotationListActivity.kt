package vcmsa.projects.fkj_consultants.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.R.id.recyclerViewQuotations
import vcmsa.projects.fkj_consultants.adapters.QuotationAdapter
import vcmsa.projects.fkj_consultants.models.Quotation

class QuotationListActivity : AppCompatActivity() {

    private lateinit var recyclerViewQuotations: RecyclerView
    private lateinit var adapter: QuotationAdapter
    private val quotations = mutableListOf<Quotation>()
    private val dbRef = FirebaseDatabase.getInstance().getReference("quotations")

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quotation_list)

        recyclerViewQuotations = findViewById(R.id.recyclerViewQuotations)
        recyclerViewQuotations.layoutManager = LinearLayoutManager(this)

        adapter = QuotationAdapter(quotations) { quotation ->
            val intent = Intent(this, QuotationViewActivity::class.java)
            intent.putExtra("quotationId", quotation.quotationId)
            startActivity(intent)
        }
        recyclerViewQuotations.adapter = adapter

        loadQuotations()
    }

    private fun loadQuotations() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                quotations.clear()
                for (child in snapshot.children) {
                    val quotation = child.getValue(Quotation::class.java)
                    if (quotation != null) quotations.add(quotation)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@QuotationListActivity, "Failed to load", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
