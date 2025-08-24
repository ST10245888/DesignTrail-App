package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.Quotation

class QuotationPreviewActivity : AppCompatActivity() {

    private lateinit var img: ImageView
    private lateinit var btnShare: Button
    private lateinit var btnOpen: Button
    private lateinit var btnHistory: Button

    private var pdfUri: Uri? = null
    private var quotation: Quotation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_quotation)

        img = findViewById(R.id.imgPdfPreview)
        btnShare = findViewById(R.id.btnSharePdf)
        btnOpen = findViewById(R.id.btnOpenPdf)
        btnHistory = findViewById(R.id.btnGoHistory)

        pdfUri = intent.getStringExtra("pdf_uri")?.let { Uri.parse(it) }
        quotation = intent.getParcelableExtra("quotation")

        renderFirstPage(pdfUri)

        btnShare.setOnClickListener {
            pdfUri?.let {
                val share = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, it)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(share, "Share quotation PDF"))
            }
        }

        btnOpen.setOnClickListener {
            pdfUri?.let {
                val view = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(it, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                runCatching { startActivity(view) }.onFailure {
                    Toast.makeText(this, "No PDF viewer found", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnHistory.setOnClickListener {
            startActivity(Intent(this, QuotationHistoryActivity::class.java))
        }
    }

    private fun renderFirstPage(uri: Uri?) {
        if (uri == null) return
        try {
            val pfd: ParcelFileDescriptor? = contentResolver.openFileDescriptor(uri, "r")
            val renderer = PdfRenderer(pfd!!)
            val page = renderer.openPage(0)
            val bmp = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            img.setImageBitmap(bmp)
            page.close()
            renderer.close()
            pfd.close()
        } catch (e: Exception) {
            Toast.makeText(this, "Preview failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
