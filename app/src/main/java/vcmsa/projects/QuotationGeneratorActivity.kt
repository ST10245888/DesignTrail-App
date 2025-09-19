package vcmsa.projects.fkj_consultants.activities

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.BasketItem
import vcmsa.projects.fkj_consultants.models.Quotation
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class QuotationGeneratorActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var etCompany: EditText
    private lateinit var etRequester: EditText
    private lateinit var etEmail: EditText
    private lateinit var etTel: EditText
    private lateinit var etAddress: EditText
    private lateinit var etBillTo: EditText
    private lateinit var btnGenerate: Button

    private val basket = mutableListOf<BasketItem>()
    private val nf = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_quotation)

        recycler = findViewById(R.id.recyclerViewProducts)
        tvTotal = findViewById(R.id.tvTotal)
        etCompany = findViewById(R.id.etCompanyName)
        etRequester = findViewById(R.id.etRequesterName)
        etEmail = findViewById(R.id.etEmail)
        etTel = findViewById(R.id.etTel)
        etAddress = findViewById(R.id.etAddress)
        etBillTo = findViewById(R.id.etBillTo)
        btnGenerate = findViewById(R.id.btnGeneratePdf)

        // Receive basket items
        intent.getParcelableArrayListExtra<BasketItem>("basket_items")?.let { basket.addAll(it) }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = ProductAdapter(basket)

        updateTotal()

        // Request legacy storage permission for API <= 28
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                900
            )
        }

        btnGenerate.setOnClickListener {
            if (basket.isEmpty()) {
                Toast.makeText(this, "Basket is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (etRequester.text.isNullOrBlank()) { etRequester.error = "Required"; return@setOnClickListener }
            if (etEmail.text.isNullOrBlank()) { etEmail.error = "Required"; return@setOnClickListener }

            // Create Quotation object
            val total = basket.sumOf { it.product.price * it.quantity }  // ✅ fixed
            val quotation = Quotation(
                quotationId = System.currentTimeMillis().toString(),
                companyName = etCompany.text.toString(),
                requesterName = etRequester.text.toString(),
                email = etEmail.text.toString(),
                tel = etTel.text.toString(),
                address = etAddress.text.toString(),
                billTo = etBillTo.text.toString(),
                products = basket.toList(),
                total = total
            )

            val pdfUri = generatePdfToDownloads(quotation)
            if (pdfUri != null) {
                val i = Intent(this, QuotationPreviewActivity::class.java)
                i.putExtra("quotation", quotation)
                i.putExtra("pdf_uri", pdfUri.toString())
                startActivity(i)
            }
        }
    }

    private fun updateTotal() {
        val total = basket.sumOf { it.product.price * it.quantity }  // ✅ fixed
        tvTotal.text = "Total: ${nf.format(total)}"
    }

    private fun generatePdfToDownloads(q: Quotation): Uri? {
        // ... same code as before, just replace it.material → it.product everywhere
        val pageWidth = 595
        val pageHeight = 842
        val doc = PdfDocument()
        val title = Paint().apply { textSize = 18f; typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD) }
        val label = Paint().apply { textSize = 12f; isAntiAlias = true }
        val bold = Paint().apply { textSize = 12f; typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD) }
        val line = Paint().apply { strokeWidth = 1f }

        fun newPage(): PdfDocument.Page {
            val info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, doc.pages.size + 1).create()
            return doc.startPage(info)
        }

        var page = newPage()
        var canvas = page.canvas
        var y = 50

        runCatching {
            val bmp = BitmapFactory.decodeResource(resources, R.drawable.ic_fkj_logo)
            bmp?.let { canvas.drawBitmap(Bitmap.createScaledBitmap(it, 90, 90, true), 40f, 30f, null) }
        }

        canvas.drawText("QUOTATION", 150f, 60f, title)
        val ts = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(q.timestamp))
        canvas.drawText("Date: $ts", 150f, 80f, label)

        y = 120
        canvas.drawText("Company: ${q.companyName}", 40f, y.toFloat(), label); y += 18
        canvas.drawText("Requester: ${q.requesterName}", 40f, y.toFloat(), label); y += 18
        canvas.drawText("Email: ${q.email}", 40f, y.toFloat(), label); y += 18
        canvas.drawText("Tel: ${q.tel}", 40f, y.toFloat(), label); y += 18
        canvas.drawText("Address: ${q.address}", 40f, y.toFloat(), label); y += 18
        canvas.drawText("Bill To: ${q.billTo}", 40f, y.toFloat(), label); y += 24

        canvas.drawLine(40f, y.toFloat(), (pageWidth - 40).toFloat(), y.toFloat(), line); y += 14
        canvas.drawText("Item", 40f, y.toFloat(), bold)
        canvas.drawText("Qty", 300f, y.toFloat(), bold)
        canvas.drawText("Price", 360f, y.toFloat(), bold)
        canvas.drawText("Subtotal", 440f, y.toFloat(), bold); y += 10
        canvas.drawLine(40f, y.toFloat(), (pageWidth - 40).toFloat(), y.toFloat(), line); y += 16

        fun ensureSpace(h: Int = 18) {
            if (y + h > pageHeight - 80) {
                doc.finishPage(page)
                page = newPage()
                canvas = page.canvas
                y = 50
                canvas.drawText("Item", 40f, y.toFloat(), bold)
                canvas.drawText("Qty", 300f, y.toFloat(), bold)
                canvas.drawText("Price", 360f, y.toFloat(), bold)
                canvas.drawText("Subtotal", 440f, y.toFloat(), bold); y += 10
                canvas.drawLine(40f, y.toFloat(), (pageWidth - 40).toFloat(), y.toFloat(), line); y += 16
            }
        }

        q.products.forEach {
            ensureSpace()
            canvas.drawText(it.product.name, 40f, y.toFloat(), label)
            canvas.drawText("x${it.quantity}", 300f, y.toFloat(), label)
            canvas.drawText(nf.format(it.product.price), 360f, y.toFloat(), label)
            canvas.drawText(nf.format(it.product.price * it.quantity), 440f, y.toFloat(), label)
            y += 18
            it.selectedColor?.let { c -> if (c.isNotBlank()) { ensureSpace(); canvas.drawText("   Color: $c", 40f, y.toFloat(), label); y += 16 } }
            it.selectedSize?.let { s -> if (s.isNotBlank()) { ensureSpace(); canvas.drawText("   Size: $s", 40f, y.toFloat(), label); y += 16 } }
        }

        y += 10
        canvas.drawLine(40f, y.toFloat(), (pageWidth - 40).toFloat(), y.toFloat(), line); y += 24
        canvas.drawText("TOTAL: ${nf.format(q.total)}", 40f, y.toFloat(), bold)

        doc.finishPage(page)

        val displayName = "Quotation_${q.quotationId}.pdf"

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, displayName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                contentResolver.openOutputStream(uri!!)?.use { doc.writeTo(it) }
                values.clear(); values.put(MediaStore.Downloads.IS_PENDING, 0)
                contentResolver.update(uri!!, values, null, null)
                Toast.makeText(this, "Saved to Downloads/$displayName", Toast.LENGTH_LONG).show()
                uri
            } else {
                @Suppress("DEPRECATION")
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, displayName)
                FileOutputStream(file).use { doc.writeTo(it) }
                Toast.makeText(this, "Saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                Uri.fromFile(file)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to save PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        } finally {
            doc.close()
        }
    }

    class ProductAdapter(private val items: List<BasketItem>) :
        RecyclerView.Adapter<ProductAdapter.VH>() {
        inner class VH(v: android.view.View) : RecyclerView.ViewHolder(v) {
            val n: TextView = v.findViewById(R.id.tvBasketItemName)
            val d: TextView = v.findViewById(R.id.tvBasketItemDetails)
            val p: TextView = v.findViewById(R.id.tvBasketItemPrice)
        }

        override fun onCreateViewHolder(p: android.view.ViewGroup, vt: Int): VH {
            val v = android.view.LayoutInflater.from(p.context).inflate(R.layout.item_basket, p, false)
            return VH(v)
        }

        override fun onBindViewHolder(h: VH, i: Int) {
            val it = items[i]
            h.n.text = it.product.name
            h.d.text = "Color: ${it.selectedColor ?: "N/A"}, Size: ${it.selectedSize ?: "N/A"}, Qty: ${it.quantity}"
            h.p.text = "R %.2f".format(it.product.price * it.quantity)
        }

        override fun getItemCount() = items.size
    }
}
