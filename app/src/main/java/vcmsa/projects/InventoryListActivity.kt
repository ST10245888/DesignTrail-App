package vcmsa.projects.fkj_consultants.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.adapters.InventoryAdapter
import vcmsa.projects.fkj_consultants.models.Product

class InventoryListActivity : AppCompatActivity(), InventoryAdapter.InventoryListener {

    private lateinit var recycler: RecyclerView
    private val items = mutableListOf<Product>()
    private lateinit var adapter: InventoryAdapter
    private val dbRef = FirebaseDatabase.getInstance().getReference("inventory")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_list)

        recycler = findViewById(R.id.recyclerInventory)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = InventoryAdapter(items, this)
        recycler.adapter = adapter

        loadInventory()
    }

    private fun loadInventory() {
        dbRef.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                items.clear()
                for (s in snapshot.children) {
                    val p = s.getValue(Product::class.java)
                    p?.let { items.add(0, it) } // newest first
                }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@InventoryListActivity, "Failed to load inventory", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // InventoryAdapter callbacks:
    override fun onEdit(product: Product) {
        val i = Intent(this, InventoryActivity::class.java)
        i.putExtra("edit_product", product)
        startActivity(i)
    }

    override fun onDelete(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Delete product?")
            .setMessage("Remove ${product.name} from inventory?")
            .setPositiveButton("Delete") { _, _ ->
                dbRef.child(product.productId).removeValue()
                    .addOnSuccessListener { Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show() }
                    .addOnFailureListener { Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show() }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onToggleAvailability(product: Product) {
        val newVal = if (product.availability == "In Stock") "Out of Stock" else "In Stock"
        dbRef.child(product.productId).child("availability").setValue(newVal)
            .addOnSuccessListener { Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show() }
            .addOnFailureListener { Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show() }
    }

    // add search in toolbar menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.inventory_menu, menu)
        val search = menu?.findItem(R.id.action_search)?.actionView as? SearchView
        search?.queryHint = "Search inventory..."
        search?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText ?: "")
                return true
            }
        })
        return true
    }
}
