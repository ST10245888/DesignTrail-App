package vcmsa.projects.fkj_consultants.activities

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import vcmsa.projects.fkj_consultants.R
import java.io.File

class QuotationViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quotation_viewer)

        val txtViewer = findViewById<TextView>(R.id.txtQuotationContent)

        val filePath = intent.getStringExtra("filePath")
        if (filePath != null) {
            val file = File(filePath)
            if (file.exists()) {
                val content = file.readText()
                txtViewer.text = content
            } else {
                Toast.makeText(this, "Quotation file not found!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No file path provided", Toast.LENGTH_SHORT).show()
        }
    }
}
