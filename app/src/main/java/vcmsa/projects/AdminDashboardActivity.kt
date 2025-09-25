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
            startActivity(Intent(this, AdminQuotationListActivity::class.java))
        }

        btnInventory.setOnClickListener {
            startActivity(Intent(this, InventoryActivity::class.java))
        }

        btnMessaging.setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java))
        }

        // Bottom navigation handling
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> true
                R.id.nav_catalog -> true
                R.id.nav_dashboard -> true
                R.id.nav_messages -> {
                    startActivity(Intent(this, ChatListActivity::class.java))
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }
}