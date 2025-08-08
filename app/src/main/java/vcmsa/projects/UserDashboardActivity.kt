package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.activities.LoginActivity

class UserDashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_dashboard)

        auth = FirebaseAuth.getInstance()

        val btnCatalog = findViewById<Button>(R.id.btnCatalog)
        //val btnQuotations = findViewById<Button>(R.id.btnQuotations)
        val btnMessages = findViewById<Button>(R.id.btnMessages)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        bottomNav = findViewById(R.id.bottomNavigation)
        val btnQuotations = findViewById<Button>(R.id.btnQuotations)

                // Buttons in layout
        btnCatalog.setOnClickListener {

            Toast.makeText(this, "Catalog Clicked", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, CatalogActivity::class.java))
        // To record information about the execution of a program (Kotlin & Spring Boot Guide, 2024)
        }



        btnQuotations.setOnClickListener {
          Toast.makeText(this, "Quotations Clicked", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, QuotationGeneratorActivity::class.java))
        }

        btnMessages.setOnClickListener {
            Toast.makeText(this, "Messaging Clicked", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MessagingActivity::class.java))
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