package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import vcmsa.projects.fkj_consultants.R

class BrandingActivity : AppCompatActivity() {

    private lateinit var btnQuotePrinting: Button
    private lateinit var btnQuoteEmbroidery: Button
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_branding)

        // Buttons
        btnQuotePrinting = findViewById(R.id.btnQuotePrinting)
        btnQuoteEmbroidery = findViewById(R.id.btnQuoteEmbroidery)

        btnQuotePrinting.setOnClickListener {
            val intent = Intent(this, QuoteRequestActivity::class.java)
            intent.putExtra("SERVICE_TYPE", "Printing")
            startActivity(intent)
        }

        btnQuoteEmbroidery.setOnClickListener {
            val intent = Intent(this, QuoteRequestActivity::class.java)
            intent.putExtra("SERVICE_TYPE", "Embroidery")
            startActivity(intent)
        }

        // Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, UserDashboardActivity::class.java))
                    true
                }
                R.id.nav_catalog -> {
                    startActivity(Intent(this, CatalogActivity::class.java))
                    true
                }
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, UserDashboardActivity::class.java))
                    true
                }
                R.id.nav_messages -> {
                    startActivity(Intent(this, ChatActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileAccountActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
