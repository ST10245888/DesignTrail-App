package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.Quotation
import vcmsa.projects.fkj_consultants.models.QuotationItem
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

data class ColourOption(val name: String, val hex: String)

class QuoteRequestActivity : AppCompatActivity() {

    private lateinit var txtServiceType: TextView
    private lateinit var edtQuantity: EditText
    private lateinit var spinnerColors: Spinner
    private lateinit var edtNotes: EditText
    private lateinit var edtCompany: EditText
    private lateinit var edtAddress: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPhone: EditText
    private lateinit var edtBillTo: EditText
    private lateinit var btnUploadDesign: Button
    private lateinit var btnSubmit: Button
    private lateinit var txtFileSelected: TextView

    private var selectedFileUri: Uri? = null
    private var serviceType: String = ""

    private val pricePerItem = 190.0

    private val colors = listOf(
        ColourOption("Red", "#FF0000"), ColourOption("Green", "#00FF00"), ColourOption("Blue", "#0000FF"),
        ColourOption("Yellow", "#FFFF00"), ColourOption("Orange", "#FFA500"), ColourOption("Purple", "#800080"),
        ColourOption("Pink", "#FFC0CB"), ColourOption("Brown", "#A52A2A"), ColourOption("Gray", "#808080"),
        ColourOption("Black", "#000000"), ColourOption("White", "#FFFFFF"), ColourOption("Cyan", "#00FFFF"),
        ColourOption("Magenta", "#FF00FF"), ColourOption("Teal", "#008080"), ColourOption("Olive", "#808000"),
        ColourOption("Navy", "#000080"), ColourOption("Maroon", "#800000"), ColourOption("Lime", "#00FF00"),
        ColourOption("Silver", "#C0C0C0"), ColourOption("Gold", "#FFD700")
    )

    private val filePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            txtFileSelected.text = "File Selected: ${uri.lastPathSegment}"
        } else {
            txtFileSelected.text = "No file selected"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quote_request)

        txtServiceType = findViewById(R.id.txtServiceType)
        edtQuantity = findViewById(R.id.edtQuantity)
        spinnerColors = findViewById(R.id.spinnerColors)
        edtNotes = findViewById(R.id.edtNotes)
        edtCompany = findViewById(R.id.edtCompanyName)
        edtAddress = findViewById(R.id.edtAddress)
        edtEmail = findViewById(R.id.edtEmail)
        edtPhone = findViewById(R.id.edtPhone)
        edtBillTo = findViewById(R.id.edtBillTo)
        btnUploadDesign = findViewById(R.id.btnUploadDesign)
        btnSubmit = findViewById(R.id.btnSubmit)
        txtFileSelected = findViewById(R.id.txtFileSelected)

        serviceType = intent.getStringExtra("SERVICE_TYPE") ?: "Branding"
        txtServiceType.text = "Quote Request: $serviceType"

        setupColorSpinner()

        btnUploadDesign.setOnClickListener { filePicker.launch("image/*") }
        btnSubmit.setOnClickListener { saveQuotationAndUpload() }
    }

    private fun setupColorSpinner() {
        val adapter = object : ArrayAdapter<ColourOption>(this, R.layout.item_colour_spinner, colors) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.item_colour_spinner, parent, false)
                val colorView = view.findViewById<View>(R.id.viewColor)
                val txtColor = view.findViewById<TextView>(R.id.txtColorName)
                val item = getItem(position)
                if (item != null) {
                    colorView.setBackgroundColor(android.graphics.Color.parseColor(item.hex))
                    txtColor.text = item.name
                }
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.item_colour_spinner, parent, false)
                val colorView = view.findViewById<View>(R.id.viewColor)
                val txtColor = view.findViewById<TextView>(R.id.txtColorName)
                val item = getItem(position)
                if (item != null) {
                    colorView.setBackgroundColor(android.graphics.Color.parseColor(item.hex))
                    txtColor.text = item.name
                }
                return view
            }
        }
        spinnerColors.adapter = adapter
    }

    private fun saveQuotationAndUpload() {
        val quantityStr = edtQuantity.text.toString().trim()
        val notes = edtNotes.text.toString().trim()
        val company = edtCompany.text.toString().trim()
        val address = edtAddress.text.toString().trim()
        val email = edtEmail.text.toString().trim()
        val phone = edtPhone.text.toString().trim()
        val billTo = edtBillTo.text.toString().trim()
        val color = (spinnerColors.selectedItem as ColourOption).name

        if (quantityStr.isEmpty() || company.isEmpty()) {
            Toast.makeText(this, "Please fill in required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val quantity = quantityStr.toIntOrNull()
        if (quantity == null || quantity <= 0) {
            Toast.makeText(this, "Quantity must be a valid positive number", Toast.LENGTH_SHORT).show()
            return
        }

        val subtotal = quantity * pricePerItem
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        val subtotalFormatted = currencyFormat.format(subtotal)
        val timestamp = System.currentTimeMillis()
        val dateFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))

        try {
            val dir = File(getExternalFilesDir(null), "quotations")
            if (!dir.exists()) dir.mkdirs()

            val fileName = "quotation_$timestamp.pdf"
            val file = File(dir, fileName)

            // --- Generate PDF with user image ---
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas
            val paint = Paint()

            // Header
            paint.textSize = 18f
            paint.isFakeBoldText = true
            canvas.drawText("FKJ CONSULTANTS - Quotation", 40f, 50f, paint)

            paint.textSize = 14f
            paint.isFakeBoldText = false
            var y = 90f
            fun drawLine(label: String, value: String) {
                canvas.drawText("$label $value", 40f, y, paint)
                y += 25f
            }

            drawLine("Date:", dateFormatted)
            drawLine("Company:", company)
            drawLine("Address:", address)
            drawLine("Email:", email)
            drawLine("Phone:", phone)
            drawLine("Bill To:", billTo)
            drawLine("Service:", serviceType)
            drawLine("Quantity:", quantity.toString())
            drawLine("Color:", color)
            drawLine("Notes:", notes)
            drawLine("Price per Item:", currencyFormat.format(pricePerItem))
            drawLine("Subtotal:", subtotalFormatted)

            // Draw uploaded image
            selectedFileUri?.let { uri ->
                contentResolver.openInputStream(uri)?.use { input ->
                    val bitmap = BitmapFactory.decodeStream(input)
                    bitmap?.let {
                        val scaled = Bitmap.createScaledBitmap(it, 200, 200, true)
                        canvas.drawBitmap(scaled, 40f, y + 20f, paint)
                        y += 240f
                    }
                }
            }

            pdfDocument.finishPage(page)
            pdfDocument.writeTo(file.outputStream())
            pdfDocument.close()

            // --- Upload to Firebase ---
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser ?: run {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return
            }

            val userId = currentUser.uid
            val dbRef = FirebaseDatabase.getInstance().getReference("quotations")
            val quotationId = dbRef.push().key ?: UUID.randomUUID().toString()

            val quotationItem = QuotationItem(
                productId = "ITEM-$timestamp",
                name = serviceType,
                pricePerUnit = pricePerItem,
                quantity = quantity,
                color = color,
                description = notes
            )

            val quotation = Quotation(
                id = quotationId,
                userId = userId,
                companyName = company,
                address = address,
                email = email,
                phone = phone,
                billTo = billTo,
                serviceType = serviceType,
                color = color,
                notes = notes,
                quantity = quantity,
                items = listOf(quotationItem),
                fileName = fileName,
                filePath = file.absolutePath,
                timestamp = timestamp,
                status = "Pending"
            )

            dbRef.child(quotationId).setValue(quotation)
                .addOnSuccessListener {
                    AlertDialog.Builder(this)
                        .setTitle("Quotation Submitted")
                        .setMessage("Quotation successfully submitted!\nSubtotal: $subtotalFormatted")
                        .setPositiveButton("View My Quotations") { _, _ ->
                            startActivity(Intent(this, QuotationActivity::class.java))
                            finish()
                        }
                        .setNegativeButton("Close", null)
                        .show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to upload: ${e.message}", Toast.LENGTH_LONG).show()
                }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving quotation: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
// (Android Developers, 2025).
