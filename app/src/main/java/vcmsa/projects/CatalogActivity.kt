package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import vcmsa.projects.fkj_consultants.R

class CatalogActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)

        // Button bindings
        val btnBranding = findViewById<Button>(R.id.btnBranding)
        val btnPromotionalMaterials = findViewById<Button>(R.id.btnPromotionalMaterials)
        val btnMessaging = findViewById<Button>(R.id.btnMessaging)

        bottomNav = findViewById(R.id.bottomNavigation)

        btnBranding.setOnClickListener {
            Toast.makeText(this, "Branding Selected", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, BrandingActivity::class.java))
        }

        btnPromotionalMaterials.setOnClickListener {
            Toast.makeText(this, "Promotional Materials Selected", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, SelectMaterialActivity::class.java))
        }

        btnMessaging.setOnClickListener {
            Toast.makeText(this, "Messaging Selected", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MessagingActivity::class.java))
        }

        // Bottom Navigation listener
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, UserDashboardActivity::class.java))
                    true
                }
                R.id.nav_catalog -> {
                    Toast.makeText(this, "Already on Catalog", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_dashboard -> {
                    // Could navigate to an admin or analytics dashboard if implemented
                    Toast.makeText(this, "Dashboard Selected", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_messages -> {
                    startActivity(Intent(this, MessagingActivity::class.java))
                    true
                }
               // R.id.nav_profile -> {
//startActivity(Intent(this, ProfileActivity::class.java))
                 //   true
                //}
                else -> false
            }
        }
    }
}
