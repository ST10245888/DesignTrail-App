package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import vcmsa.projects.fkj_consultants.R

class UserDashboardActivity : AppCompatActivity() {

    companion object {
        // Replace with your actual Firebase admin UID
        private const val ADMIN_UID = "Mavuso2@gmail.com"
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_dashboard)

        auth = FirebaseAuth.getInstance()

        val btnCatalog = findViewById<Button>(R.id.btnCatalog)
        val btnMessages = findViewById<Button>(R.id.btnMessages)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        bottomNav = findViewById(R.id.bottomNavigation)
        val btnQuotations = findViewById<Button>(R.id.btnQuotations)

        // Buttons in layout
        btnCatalog.setOnClickListener {
            startActivity(Intent(this, CatalogActivity::class.java))
        }

        btnQuotations.setOnClickListener {
            startActivity(Intent(this, QuotationActivity::class.java))
        }

        btnMessages.setOnClickListener {
            Toast.makeText(this, "Messaging Clicked", Toast.LENGTH_SHORT).show()

            val currentUserId = auth.currentUser?.uid
            if (currentUserId != null) {
                if (currentUserId == ADMIN_UID) {
                    startActivity(Intent(this, ChatListActivity::class.java))
                } else {
                    startActivity(Intent(this, ChatActivity::class.java).apply {
                        putExtra("receiverId", ADMIN_UID)
                    })
                }
            } else {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            }
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Bottom Navigation
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
