package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import vcmsa.projects.fkj_consultants.R

class BrandingActivity : AppCompatActivity() {

    private lateinit var sectionPrinting: LinearLayout
    private lateinit var sectionEmbroidery: LinearLayout
    private lateinit var btnPrinting: Button
    private lateinit var btnEmbroidery: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_branding)

        sectionPrinting = findViewById(R.id.sectionPrinting)
        sectionEmbroidery = findViewById(R.id.sectionEmbroidery)
        btnPrinting = findViewById(R.id.btnShowPrinting)
        btnEmbroidery = findViewById(R.id.btnShowEmbroidery)

        // Toggle between Printing and Embroidery
        btnPrinting.setOnClickListener {
            sectionPrinting.visibility = View.VISIBLE
            sectionEmbroidery.visibility = View.GONE
        }

        btnEmbroidery.setOnClickListener {
            sectionEmbroidery.visibility = View.VISIBLE
            sectionPrinting.visibility = View.GONE
        }

        // Open QuoteRequestActivity for Printing
        val btnQuotePrinting = findViewById<Button>(R.id.btnQuotePrinting)
        btnQuotePrinting.setOnClickListener {
            val intent = Intent(this, QuoteRequestActivity::class.java)
            intent.putExtra("SERVICE_TYPE", "Printing")
            startActivity(intent)
        }

        // Open QuoteRequestActivity for Embroidery
        val btnQuoteEmbroidery = findViewById<Button>(R.id.btnQuoteEmbroidery)
        btnQuoteEmbroidery.setOnClickListener {
            val intent = Intent(this, QuoteRequestActivity::class.java)
            intent.putExtra("SERVICE_TYPE", "Embroidery")
            startActivity(intent)
        }
    }
}
