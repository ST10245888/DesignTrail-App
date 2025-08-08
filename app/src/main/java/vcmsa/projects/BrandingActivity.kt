package vcmsa.projects.fkj_consultants.activities


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import vcmsa.projects.fkj_consultants.R

class BrandingActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_branding)

        val btnPrinting = findViewById<Button>(R.id.btnPrinting)
        val btnEmbroidery = findViewById<Button>(R.id.btnEmbroidery)
        bottomNav = findViewById(R.id.bottomNavigation)

        btnPrinting.setOnClickListener {
            Toast.makeText(this, "Printing selected", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, SelectMaterialActivity::class.java)
            intent.putExtra("brandingType", "Printing")
            startActivity(intent)
        }

        btnEmbroidery.setOnClickListener {
            Toast.makeText(this, "Embroidery selected", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, SelectMaterialActivity::class.java)
            intent.putExtra("brandingType", "Embroidery")
            startActivity(intent)
        }

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, UserDashboardActivity::class.java))
                    true
                }
                R.id.nav_catalog -> {
                    startActivity(Intent(this, CatalogActivity::class.java))
                    true
                }
                R.id.nav_messages -> {
                    startActivity(Intent(this, MessagingActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
