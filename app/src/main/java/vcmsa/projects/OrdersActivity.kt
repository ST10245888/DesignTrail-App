package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.Quotation
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class OrdersActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var ordersContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoOrders: TextView
    private val orders = mutableListOf<Quotation>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        ordersContainer = findViewById(R.id.ordersContainer)
        progressBar = findViewById(R.id.progressBar)
        tvNoOrders = findViewById(R.id.tvNoOrders)

        loadOrdersFromFirestore()
    }

    private fun loadOrdersFromFirestore() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
            tvNoOrders.visibility = View.VISIBLE
            return
        }

        progressBar.visibility = View.VISIBLE
        tvNoOrders.visibility = View.GONE

        firestore.collection("quotations")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                progressBar.visibility = View.GONE
                orders.clear()
                ordersContainer.removeAllViews()

                if (documents.isEmpty) {
                    tvNoOrders.visibility = View.VISIBLE
                    tvNoOrders.text = "No orders found. Start by adding items to your cart!"
                    return@addOnSuccessListener
                }

                for (doc in documents) {
                    val order = doc.toObject(Quotation::class.java)
                    orders.add(order)
                    addOrderCard(order)
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                tvNoOrders.visibility = View.VISIBLE
                tvNoOrders.text = "Error loading orders: ${e.message}"
                Toast.makeText(this, "Error loading orders: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addOrderCard(order: Quotation) {
        val cardView = LayoutInflater.from(this)
            .inflate(R.layout.item_order_card, ordersContainer, false) as CardView

        val orderSummary: LinearLayout = cardView.findViewById(R.id.orderSummary)
        val orderDetails: LinearLayout = cardView.findViewById(R.id.orderDetails)
        val tvOrderNumber: TextView = cardView.findViewById(R.id.tvOrderNumber)
        val tvOrderItems: TextView = cardView.findViewById(R.id.tvOrderItems)
        val tvOrderStatus: TextView = cardView.findViewById(R.id.tvOrderStatus)
        val tvOrderDate: TextView = cardView.findViewById(R.id.tvOrderDate)
        val tvExpandHint: TextView = cardView.findViewById(R.id.tvExpandHint)

        val tvDetailCompany: TextView = cardView.findViewById(R.id.tvDetailCompany)
        val tvDetailEmail: TextView = cardView.findViewById(R.id.tvDetailEmail)
        val tvDetailPhone: TextView = cardView.findViewById(R.id.tvDetailPhone)
        val tvDetailAddress: TextView = cardView.findViewById(R.id.tvDetailAddress)
        val tvDetailTotal: TextView = cardView.findViewById(R.id.tvDetailTotal)
        val tvDetailItemsList: TextView = cardView.findViewById(R.id.tvDetailItemsList)

        val btnView: View = cardView.findViewById(R.id.btnViewOrder)
        val btnDownload: View = cardView.findViewById(R.id.btnDownloadOrder)
        val btnSend: View = cardView.findViewById(R.id.btnSendOrder)

        tvOrderNumber.text = "Order: ${order.fileName.replace("quotation_", "").replace(".txt", "")}"
        tvOrderDate.text = "Date: ${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(order.timestamp))}"
        tvOrderStatus.text = "Status: ${order.status}"
        when (order.status.lowercase()) {
            "pending" -> tvOrderStatus.setTextColor(getColor(android.R.color.holo_orange_dark))
            "approved" -> tvOrderStatus.setTextColor(getColor(android.R.color.holo_green_dark))
            "rejected" -> tvOrderStatus.setTextColor(getColor(android.R.color.holo_red_dark))
        }

        tvOrderItems.text = buildItemsSummary(order)
        tvDetailCompany.text = "Company: ${order.companyName}"
        tvDetailEmail.text = "Email: ${order.email}"
        tvDetailPhone.text = "Phone: ${order.phone}"
        tvDetailAddress.text = "Address: ${order.address}"
        tvDetailTotal.text = "Total: R${String.format("%.2f", order.subtotal)}"
        tvDetailItemsList.text = buildDetailedItemsList(order)

        orderSummary.setOnClickListener {
            if (orderDetails.visibility == View.GONE) {
                orderDetails.visibility = View.VISIBLE
                tvExpandHint.text = "Tap to collapse"
            } else {
                orderDetails.visibility = View.GONE
                tvExpandHint.text = "Tap to expand"
            }
        }

        btnView.setOnClickListener { viewOrder(order) }
        btnDownload.setOnClickListener { downloadOrder(order) }
        btnSend.setOnClickListener { sendOrderToAdmin(order) }

        ordersContainer.addView(cardView)
    }

    private fun buildItemsSummary(order: Quotation): String {
        return when {
            order.type == "promotional_materials" && order.items.isNotEmpty() -> {
                val itemCount = order.items.size
                val firstItem = order.items[0].name
                if (itemCount > 1) "$firstItem + ${itemCount - 1} more" else firstItem
            }
            order.type == "quote_request" && order.serviceType != null -> {
                "${order.serviceType} - ${order.quantity ?: 0} units"
            }
            else -> "Custom Order - R${String.format("%.2f", order.subtotal)}"
        }
    }

    private fun buildDetailedItemsList(order: Quotation): String {
        val builder = StringBuilder()
        builder.append("Items:\n\n")
        when {
            order.type == "promotional_materials" && order.items.isNotEmpty() -> {
                order.items.forEachIndexed { index, item ->
                    val total = item.pricePerUnit * item.quantity
                    builder.append("${index + 1}. ${item.name}\n")
                    builder.append("   Quantity: ${item.quantity}\n")
                    builder.append("   Price: R${String.format("%.2f", item.pricePerUnit)} each\n")
                    builder.append("   Subtotal: R${String.format("%.2f", total)}\n\n")
                }
            }
            order.type == "quote_request" -> {
                builder.append("Service: ${order.serviceType ?: "N/A"}\n")
                builder.append("Quantity: ${order.quantity ?: 0}\n")
                builder.append("Color: ${order.color ?: "N/A"}\n")
                if (!order.notes.isNullOrEmpty()) builder.append("Notes: ${order.notes}\n")
                if (!order.designFileUrl.isNullOrEmpty()) builder.append("Design: Attached\n")
            }
            else -> builder.append("Custom order details\n")
        }
        return builder.toString()
    }

    private fun viewOrder(order: Quotation) {
        val file = File(order.filePath)
        if (!file.exists()) {
            Toast.makeText(this, "Order file not found", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val uri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/plain")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "View Order"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error opening file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadOrder(order: Quotation) {
        val file = File(order.filePath)
        if (!file.exists()) {
            Toast.makeText(this, "Order file not found", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/plain"
                putExtra(Intent.EXTRA_TITLE, order.fileName)
            }
            startActivity(intent)
            Toast.makeText(this, "File available at: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "File location: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }
    }

    private fun sendOrderToAdmin(order: Quotation) {
        val file = File(order.filePath)
        if (!file.exists()) {
            Toast.makeText(this, "Order file not found", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val uri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.fileprovider", file)
            val adminEmail = "admin@fkjconsultants.com"
            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(adminEmail))
                putExtra(Intent.EXTRA_SUBJECT, "Order Inquiry - ${order.companyName}")
                putExtra(Intent.EXTRA_TEXT, buildEmailBody(order))
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(emailIntent, "Send order to admin"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error sending email: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildEmailBody(order: Quotation): String {
        val builder = StringBuilder()
        builder.append("Please find my order details attached.\n\n")
        builder.append("ORDER INFORMATION\n==================\n")
        builder.append("Company: ${order.companyName}\n")
        builder.append("Email: ${order.email}\n")
        builder.append("Phone: ${order.phone}\n")
        builder.append("Status: ${order.status}\n")
        builder.append("Total: R${String.format("%.2f", order.subtotal)}\n\n")

        when {
            order.type == "promotional_materials" && order.items.isNotEmpty() -> {
                builder.append("ITEMS:\n")
                order.items.forEach { item ->
                    builder.append("- ${item.name} x ${item.quantity}\n")
                }
            }
            order.type == "quote_request" -> {
                builder.append("SERVICE:\n")
                builder.append("- ${order.serviceType ?: "N/A"}\n")
                builder.append("- Quantity: ${order.quantity ?: 0}\n")
                builder.append("- Color: ${order.color ?: "N/A"}\n")
            }
        }

        builder.append("\nI would like to inquire about this order.\n\nThank you.")
        return builder.toString()
    }

    override fun onResume() {
        super.onResume()
        loadOrdersFromFirestore()
    }
}
