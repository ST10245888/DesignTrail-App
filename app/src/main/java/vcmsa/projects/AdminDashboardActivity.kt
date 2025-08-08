package vcmsa.projects.fkj_consultants.activities
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import vcmsa.projects.fkj_consultants.R

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var bottomNav: BottomNavigationView

    private lateinit var btnUserQuotations: Button
    private lateinit var btnInventory: Button
    private lateinit var btnMessaging: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        auth = FirebaseAuth.getInstance()

        btnUserQuotations = findViewById(R.id.btnUserQuotations)
        btnInventory = findViewById(R.id.btnInventory)
        btnMessaging = findViewById(R.id.btnMessaging)
        bottomNav = findViewById(R.id.bottomNavigation)

        btnUserQuotations.setOnClickListener {
            Toast.makeText(this, "User Quotations: Coming soon", Toast.LENGTH_SHORT).show()
        }

        btnInventory.setOnClickListener {
                Toast.makeText(this, "Inventory Clicked", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, InventoryActivity::class.java))
            }

        btnMessaging.setOnClickListener {
            Toast.makeText(this, "Messaging: Coming soon", Toast.LENGTH_SHORT).show()
        }

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    Toast.makeText(this, "Home Selected", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_catalog -> {
                    Toast.makeText(this, "Catalog Selected", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_dashboard -> {
                    Toast.makeText(this, "Dashboard Selected", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_messages -> {
                    Toast.makeText(this, "Messaging Selected", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, "Profile Selected", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }
}
