package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import vcmsa.projects.fkj_consultants.R

class CatalogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)

        val btnBranding = findViewById<Button>(R.id.btnBranding)
        val btnPromotional = findViewById<Button>(R.id.btnPromotionalMaterials)
        val btnConverse = findViewById<Button>(R.id.btnMessaging)

        // Branding button action
        btnBranding.setOnClickListener {
            startActivity(Intent(this, BrandingActivity::class.java))
        }

        // Promotional Materials button action
        btnPromotional.setOnClickListener {
            startActivity(Intent(this, PromotionalMaterialsActivity::class.java))
        }

        // Converse button action
        btnConverse.setOnClickListener {
            Toast.makeText(this, "Converse Selected", Toast.LENGTH_SHORT).show()
        }
    }
}
