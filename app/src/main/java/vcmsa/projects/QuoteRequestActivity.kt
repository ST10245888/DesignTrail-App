package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.Quotation
import vcmsa.projects.fkj_consultants.models.QuotationItem
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

data class ColourOption(val name: String, val hex: String)

class QuoteRequestActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "QuoteRequestActivity"
        private const val PRICE_PER_ITEM = 190.0
    }

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
    private lateinit var txtSubtotal: TextView
    private lateinit var progressBar: ProgressBar

    private var selectedFileUri: Uri? = null
    private var serviceType: String = ""

    private val colors = listOf(
        ColourOption("Red", "#FF0000"), ColourOption("Green", "#00FF00"),
        ColourOption("Blue", "#0000FF"), ColourOption("Yellow", "#FFFF00"),
        ColourOption("Orange", "#FFA500"), ColourOption("Purple", "#800080"),
        ColourOption("Pink", "#FFC0CB"), ColourOption("Brown", "#A52A2A"),
        ColourOption("Gray", "#808080"), ColourOption("Black", "#000000"),
        ColourOption("White", "#FFFFFF"), ColourOption("Cyan", "#00FFFF"),
        ColourOption("Magenta", "#FF00FF"), ColourOption("Teal", "#008080"),
        ColourOption("Olive", "#808000"), ColourOption("Navy", "#000080"),
        ColourOption("Maroon", "#800000"), ColourOption("Lime", "#00FF00"),
        ColourOption("Silver", "#C0C0C0"), ColourOption("Gold", "#FFD700")
    )

    private val filePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            val fileName = uri.lastPathSegment ?: "Unknown File"
            txtFileSelected.text = "File Selected: $fileName"
            txtFileSelected.setTextColor(ContextCompat.getColor(this, R.color.success))
        } else {
            txtFileSelected.text = "No file selected"
            txtFileSelected.setTextColor(ContextCompat.getColor(this, R.color.textHint))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quote_request)

        initializeViews()
        setupServiceType()
        setupColorSpinner()
        setupListeners()
        setupSubtotalCalculator()
    }

    private fun initializeViews() {
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
        txtSubtotal = findViewById(R.id.txtSubtotal)
        progressBar = findViewById(R.id.progressBar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.request_quotation)
    }

    private fun setupServiceType() {
        serviceType = intent.getStringExtra("SERVICE_TYPE") ?: "Branding"
        txtServiceType.text = "${getString(R.string.service_type)}: $serviceType"
    }

    private fun setupColorSpinner() {
        val adapter = object : ArrayAdapter<ColourOption>(this, R.layout.item_colour_spinner, colors) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.item_colour_spinner, parent, false)
                bindView(view, getItem(position))
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.item_colour_spinner, parent, false)
                bindView(view, getItem(position))
                return view
            }

            private fun bindView(view: View, item: ColourOption?) {
                val colorView = view.findViewById<View>(R.id.viewColor)
                val txtColor = view.findViewById<TextView>(R.id.txtColorName)
                item?.let {
                    colorView.setBackgroundColor(android.graphics.Color.parseColor(it.hex))
                    txtColor.text = it.name
                }
            }
        }
        spinnerColors.adapter = adapter
    }

    private fun setupListeners() {
        btnUploadDesign.setOnClickListener {
            filePicker.launch("*/*") // Allow all file types
        }

        btnSubmit.setOnClickListener {
            if (validateForm()) {
                saveQuotationAndUpload()
            }
        }
    }

    private fun setupSubtotalCalculator() {
        edtQuantity.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                calculateSubtotal()
            }
        })
    }

    private fun calculateSubtotal() {
        val quantityStr = edtQuantity.text.toString().trim()
        val quantity = quantityStr.toIntOrNull() ?: 0

        if (quantity > 0) {
            val subtotal = quantity * PRICE_PER_ITEM
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
            txtSubtotal.text = "${getString(R.string.estimated_subtotal)}: ${currencyFormat.format(subtotal)}"
            txtSubtotal.setTextColor(ContextCompat.getColor(this, R.color.success))
        } else {
            txtSubtotal.text = getString(R.string.enter_quantity_to_see_subtotal)
            txtSubtotal.setTextColor(ContextCompat.getColor(this, R.color.textHint))
        }
    }

    private fun validateForm(): Boolean {
        val quantityStr = edtQuantity.text.toString().trim()
        val company = edtCompany.text.toString().trim()
        val email = edtEmail.text.toString().trim()

        if (company.isEmpty()) {
            showError("Company name is required")
            edtCompany.requestFocus()
            return false
        }

        if (quantityStr.isEmpty()) {
            showError("Quantity is required")
            edtQuantity.requestFocus()
            return false
        }

        val quantity = quantityStr.toIntOrNull()
        if (quantity == null || quantity <= 0) {
            showError("Quantity must be a valid positive number")
            edtQuantity.requestFocus()
            return false
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Valid email address is required")
            edtEmail.requestFocus()
            return false
        }

        return true
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun saveQuotationAndUpload() {
        try {
            progressBar.visibility = View.VISIBLE
            btnSubmit.isEnabled = false

            val quantity = edtQuantity.text.toString().trim().toInt()
            val notes = edtNotes.text.toString().trim()
            val company = edtCompany.text.toString().trim()
            val address = edtAddress.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val phone = edtPhone.text.toString().trim()
            val billTo = edtBillTo.text.toString().trim()
            val color = (spinnerColors.selectedItem as ColourOption).name

            val subtotal = quantity * PRICE_PER_ITEM
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
            val subtotalFormatted = currencyFormat.format(subtotal)
            val timestamp = System.currentTimeMillis()
            val dateFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))

            // Generate PDF and save locally
            val pdfFile = createQuotationPDF(
                company, address, email, phone, billTo,
                serviceType, quantity, color, notes, subtotal, dateFormatted, timestamp
            )

            if (pdfFile == null || !pdfFile.exists()) {
                throw Exception("Failed to create quotation PDF")
            }

            // Upload to Firebase
            uploadToFirebase(
                company, address, email, phone, billTo,
                serviceType, quantity, color, notes, subtotal,
                timestamp, pdfFile, currencyFormat
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error saving quotation: ${e.message}", e)
            progressBar.visibility = View.GONE
            btnSubmit.isEnabled = true
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun createQuotationPDF(
        company: String, address: String, email: String, phone: String, billTo: String,
        serviceType: String, quantity: Int, color: String, notes: String, subtotal: Double,
        dateFormatted: String, timestamp: Long
    ): File? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas
            val paint = Paint()

            var yPos = 50f
            val leftMargin = 40f
            val rightMargin = 555f
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

            // Header
            paint.textSize = 24f
            paint.color = ContextCompat.getColor(this, R.color.primary)
            paint.isFakeBoldText = true
            canvas.drawText("FKJ CONSULTANTS", leftMargin, yPos, paint)
            yPos += 30f

            paint.textSize = 18f
            paint.color = ContextCompat.getColor(this, R.color.textPrimary)
            canvas.drawText("QUOTATION REQUEST", leftMargin, yPos, paint)
            yPos += 40f

            // Date and Service Info
            paint.textSize = 12f
            paint.isFakeBoldText = false
            canvas.drawText("Date: $dateFormatted", leftMargin, yPos, paint)
            canvas.drawText("Service: $serviceType", rightMargin - 200f, yPos, paint)
            yPos += 25f

            canvas.drawText("Quotation #: Q${timestamp.toString().takeLast(6)}", leftMargin, yPos, paint)
            yPos += 30f

            // Separator
            paint.strokeWidth = 2f
            canvas.drawLine(leftMargin, yPos, rightMargin, yPos, paint)
            yPos += 20f

            // Company Details
            paint.textSize = 14f
            paint.isFakeBoldText = true
            canvas.drawText("COMPANY DETAILS", leftMargin, yPos, paint)
            yPos += 25f

            paint.textSize = 11f
            paint.isFakeBoldText = false
            drawTextLine(canvas, paint, "Company:", company, leftMargin, yPos)
            yPos += 18f
            drawTextLine(canvas, paint, "Address:", address, leftMargin, yPos)
            yPos += 18f
            drawTextLine(canvas, paint, "Email:", email, leftMargin, yPos)
            yPos += 18f
            drawTextLine(canvas, paint, "Phone:", phone, leftMargin, yPos)
            yPos += 18f
            drawTextLine(canvas, paint, "Bill To:", billTo, leftMargin, yPos)
            yPos += 30f

            // Service Details
            paint.textSize = 14f
            paint.isFakeBoldText = true
            canvas.drawText("SERVICE DETAILS", leftMargin, yPos, paint)
            yPos += 25f

            paint.textSize = 11f
            paint.isFakeBoldText = false
            drawTextLine(canvas, paint, "Service Type:", serviceType, leftMargin, yPos)
            yPos += 18f
            drawTextLine(canvas, paint, "Quantity:", quantity.toString(), leftMargin, yPos)
            yPos += 18f
            drawTextLine(canvas, paint, "Color:", color, leftMargin, yPos)
            yPos += 18f
            drawTextLine(canvas, paint, "Price per Item:", currencyFormat.format(PRICE_PER_ITEM), leftMargin, yPos)
            yPos += 18f
            drawTextLine(canvas, paint, "Subtotal:", currencyFormat.format(subtotal), leftMargin, yPos)
            yPos += 25f

            // Notes
            if (notes.isNotEmpty()) {
                paint.textSize = 14f
                paint.isFakeBoldText = true
                canvas.drawText("NOTES", leftMargin, yPos, paint)
                yPos += 25f

                paint.textSize = 11f
                paint.isFakeBoldText = false
                val notesLines = breakTextIntoLines(notes, 80)
                notesLines.forEach { line ->
                    canvas.drawText(line, leftMargin, yPos, paint)
                    yPos += 15f
                }
                yPos += 10f
            }

            // Design File
            if (selectedFileUri != null) {
                paint.textSize = 14f
                paint.isFakeBoldText = true
                canvas.drawText("DESIGN FILE ATTACHED", leftMargin, yPos, paint)
                yPos += 25f

                // Try to draw image if it's an image file
                try {
                    contentResolver.getType(selectedFileUri!!)?.takeIf { it.startsWith("image/") }?.let {
                        contentResolver.openInputStream(selectedFileUri!!)?.use { input ->
                            val bitmap = BitmapFactory.decodeStream(input)
                            bitmap?.let {
                                val scaled = Bitmap.createScaledBitmap(bitmap, 200, 200, true)
                                canvas.drawBitmap(scaled, leftMargin, yPos, paint)
                                yPos += 220f
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Could not draw design file in PDF: ${e.message}")
                }
            }

            // Footer
            yPos = 800f
            paint.textSize = 9f
            paint.color = ContextCompat.getColor(this, R.color.textHint)
            canvas.drawText("Generated by FKJ Consultants Quotation System", leftMargin, yPos, paint)

            pdfDocument.finishPage(page)

            val fileName = "Quotation_${company.replace(" ", "_")}_${timestamp}.pdf"
            val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "quotations").apply {
                if (!exists()) mkdirs()
            }
            val pdfFile = File(file, fileName)
            pdfDocument.writeTo(FileOutputStream(pdfFile))
            pdfDocument.close()

            pdfFile
        } catch (e: Exception) {
            Log.e(TAG, "Error creating PDF: ${e.message}", e)
            null
        }
    }

    private fun drawTextLine(canvas: Canvas, paint: Paint, label: String, value: String, x: Float, y: Float) {
        paint.isFakeBoldText = true
        canvas.drawText("$label ", x, y, paint)
        paint.isFakeBoldText = false
        val labelWidth = paint.measureText("$label ")
        canvas.drawText(value, x + labelWidth, y, paint)
    }

    private fun breakTextIntoLines(text: String, maxLineLength: Int): List<String> {
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        text.split(" ").forEach { word ->
            if (currentLine.length + word.length + 1 <= maxLineLength) {
                if (currentLine.isNotEmpty()) currentLine.append(" ")
                currentLine.append(word)
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            }
        }

        if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
        return lines
    }

    private fun uploadToFirebase(
        company: String, address: String, email: String, phone: String, billTo: String,
        serviceType: String, quantity: Int, color: String, notes: String, subtotal: Double,
        timestamp: Long, pdfFile: File, currencyFormat: NumberFormat
    ) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser ?: run {
            progressBar.visibility = View.GONE
            btnSubmit.isEnabled = true
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val dbRef = FirebaseDatabase.getInstance().getReference("quotations")
        val quotationId = dbRef.push().key ?: UUID.randomUUID().toString()

        val quotationItem = QuotationItem(
            productId = "ITEM-${timestamp}",
            name = serviceType,
            pricePerUnit = PRICE_PER_ITEM,
            quantity = quantity,
            color = color,
            description = notes,
            category = serviceType
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
            fileName = pdfFile.name,
            filePath = pdfFile.absolutePath,
            timestamp = timestamp,
            status = Quotation.STATUS_PENDING,
            type = "Request",
            customerName = company,
            customerEmail = email,
            designFileUrl = selectedFileUri?.toString()
        )

        dbRef.child(quotationId).setValue(quotation)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                btnSubmit.isEnabled = true
                showSuccessDialog(subtotal, currencyFormat, pdfFile)
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                btnSubmit.isEnabled = true
                Log.e(TAG, "Failed to upload quotation: ${e.message}", e)
                Toast.makeText(this, "Failed to upload: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showSuccessDialog(subtotal: Double, currencyFormat: NumberFormat, pdfFile: File) {
        val subtotalFormatted = currencyFormat.format(subtotal)

        AlertDialog.Builder(this)
            .setTitle("Quotation Submitted Successfully!")
            .setMessage("Your quotation request has been submitted and is pending review.\n\nEstimated Subtotal: $subtotalFormatted")
            .setPositiveButton("View Quotation") { _, _ ->
                // Open the PDF file
                val pdfUri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.provider",
                    pdfFile
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(pdfUri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(intent, "Open Quotation PDF"))
            }
            .setNeutralButton("View My Quotations") { _, _ ->
                startActivity(Intent(this, QuotationActivity::class.java))
                finish()
            }
            .setNegativeButton("Close") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        if (isFormDirty()) {
            AlertDialog.Builder(this)
                .setTitle("Discard Changes?")
                .setMessage("You have unsaved changes. Are you sure you want to leave?")
                .setPositiveButton("Discard") { _, _ -> super.onBackPressed() }
                .setNegativeButton("Stay", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }

    private fun isFormDirty(): Boolean {
        return edtCompany.text.isNotBlank() || edtQuantity.text.isNotBlank() ||
                edtEmail.text.isNotBlank() || selectedFileUri != null
    }
}