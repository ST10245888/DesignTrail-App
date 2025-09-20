package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import vcmsa.projects.fkj_consultants.R
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

    // Fixed price per item (R190)
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

        btnUploadDesign.setOnClickListener { filePicker.launch("*/*") }
        btnSubmit.setOnClickListener { saveQuotationLocally() }
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

    private fun saveQuotationLocally() {
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

        // Calculate subtotal
        val subtotal = quantity * pricePerItem
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        val subtotalFormatted = currencyFormat.format(subtotal)

        try {
            val dir = File(getExternalFilesDir(null), "quotations")
            if (!dir.exists()) dir.mkdirs()

            val fileName = "quotation_${System.currentTimeMillis()}.txt"
            val file = File(dir, fileName)
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

            file.bufferedWriter().use { out ->
                out.write("QUOTATION\n")
                out.write("==========================\n")
                out.write("Company: $company\n")
                out.write("Address: $address\n")
                out.write("Email: $email\n")
                out.write("Phone: $phone\n")
                out.write("Bill To: $billTo\n")
                out.write("Service: $serviceType\n")
                out.write("Quantity: $quantity\n")
                out.write("Color: $color\n")
                out.write("Notes: $notes\n")
                out.write("Price per Item: ${currencyFormat.format(pricePerItem)}\n")
                out.write("Subtotal: $subtotalFormatted\n")
                out.write("File Attached: ${selectedFileUri?.lastPathSegment ?: "None"}\n")
                out.write("Status: Pending\n")
                out.write("Date: $date\n")
            }

            AlertDialog.Builder(this)
                .setTitle("Quotation Saved")
                .setMessage("Quotation saved locally at:\n${file.absolutePath}\n\nSubtotal: $subtotalFormatted")
                .setPositiveButton("Open") { _, _ ->
                    val fileUri = androidx.core.content.FileProvider.getUriForFile(
                        this,
                        "${applicationContext.packageName}.provider",
                        file
                    )

                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(fileUri, "text/plain")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(intent, "Open Quotation"))
                }
                .setNegativeButton("Close", null)
                .show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving quotation: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
