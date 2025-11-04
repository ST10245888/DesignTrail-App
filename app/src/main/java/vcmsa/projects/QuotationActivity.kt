package vcmsa.projects.fkj_consultants.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.adapters.QuotationAdapter
import vcmsa.projects.fkj_consultants.models.ChatMessage
import vcmsa.projects.fkj_consultants.models.Quotation
import java.io.File

class QuotationActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "QuotationActivity"
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: QuotationAdapter
    private val quotations = mutableListOf<Quotation>()

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    private lateinit var chatRoot: DatabaseReference
    private lateinit var chatRef: DatabaseReference
    private var currentUserId = ""
    private var receiverId = "admin"
    private var chatId = ""

    private var quotationsListener: ValueEventListener? = null
    private var chatStatusListener: ChildEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quotation)

        setupViews()
        initializeFirebase()
        setupRecyclerView()
        setupFirebaseListener()
        setupChatListenerForStatusUpdates()
    }

    private fun setupViews() {
        // Back button setup
        val btnBack: ImageButton = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            Log.d(TAG, "Back button clicked — finishing activity")
            finish()
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        recyclerView = findViewById(R.id.recyclerQuotations)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Set up toolbar if exists
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.quotation_history)
    }

    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, getString(R.string.user_not_logged_in), Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        currentUserId = user.uid

        // Setup chat for sending quotations and receiving status updates
        chatId = if (currentUserId < receiverId) "${currentUserId}_$receiverId"
        else "${receiverId}_$currentUserId"
        chatRoot = FirebaseDatabase.getInstance().getReference("chats").child(chatId)
        chatRef = chatRoot.child("messages")

        dbRef = FirebaseDatabase.getInstance().getReference("quotations")
    }

    private fun setupRecyclerView() {
        adapter = QuotationAdapter(
            quotations,
            currentUserId,
            receiverId,
            onSendClick = ::sendQuotationToChat
        )
        recyclerView.adapter = adapter
    }

    /** Fetch all quotations for the current user and update status in real-time **/
    private fun setupFirebaseListener() {
        quotationsListener = dbRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                val updatedList = mutableListOf<Quotation>()
                snapshot.children.forEach { snap ->
                    val quotation = snap.getValue(Quotation::class.java)
                    if (quotation != null && quotation.userId == currentUserId) {
                        // Make sure id is set
                        if (quotation.id.isEmpty()) quotation.id = snap.key ?: ""
                        // Ensure customer details are populated
                        if (quotation.customerName.isEmpty()) {
                            quotation.customerName = quotation.companyName
                        }
                        if (quotation.customerEmail.isEmpty()) {
                            quotation.customerEmail = quotation.email
                        }
                        updatedList.add(quotation)
                    }
                }

                // Sort by timestamp (newest first)
                val sortedList = updatedList.sortedByDescending { it.timestamp }

                quotations.clear()
                quotations.addAll(sortedList)
                adapter.updateQuotations(sortedList)

                Log.d(TAG, "User quotations updated: ${updatedList.size}")

                // Show empty state if needed
                updateEmptyState(updatedList.isEmpty())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to fetch quotations: ${error.message}")
                Toast.makeText(
                    this@QuotationActivity,
                    getString(R.string.error_loading, error.message),
                    Toast.LENGTH_SHORT
                ).show()
                updateEmptyState(true)
            }
        })
    }

    private fun setupChatListenerForStatusUpdates() {
        // Listen for status update messages from admin
        chatStatusListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(ChatMessage::class.java)
                if (message?.senderId == "admin" && message.quotationId.isNotEmpty()) {
                    // This is a status update for a specific quotation
                    handleQuotationStatusUpdate(message)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle updated messages if needed
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Handle removed messages if needed
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle moved messages if needed
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Chat listener cancelled: ${error.message}")
            }
        }

        chatRef.addChildEventListener(chatStatusListener!!)
    }

    private fun handleQuotationStatusUpdate(message: ChatMessage) {
        // Show notification to user
        Toast.makeText(this, message.message, Toast.LENGTH_LONG).show()

        // Refresh the quotations list to show updated status
        refreshQuotations()

        Log.d(TAG, "Received status update for quotation: ${message.quotationId}")
    }

    private fun refreshQuotations() {
        // Force refresh from Firebase to get latest status
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                val updatedList = mutableListOf<Quotation>()
                snapshot.children.forEach { snap ->
                    val quotation = snap.getValue(Quotation::class.java)
                    if (quotation != null && quotation.userId == currentUserId) {
                        if (quotation.id.isEmpty()) quotation.id = snap.key ?: ""
                        if (quotation.customerName.isEmpty()) quotation.customerName = quotation.companyName
                        if (quotation.customerEmail.isEmpty()) quotation.customerEmail = quotation.email
                        updatedList.add(quotation)
                    }
                }

                val sortedList = updatedList.sortedByDescending { it.timestamp }
                quotations.clear()
                quotations.addAll(sortedList)
                adapter.updateQuotations(sortedList)

                Log.d(TAG, "Quotations refreshed after status update: ${updatedList.size} items")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Refresh failed: ${error.message}")
                Toast.makeText(
                    this@QuotationActivity,
                    getString(R.string.error_loading, error.message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        // You can add an empty state view here
        if (isEmpty) {
            Toast.makeText(this, getString(R.string.no_quotations), Toast.LENGTH_SHORT).show()
        }
    }

    /** Sends quotation to chat (admin) **/
    @SuppressLint("RestrictedApi")
    private fun sendQuotationToChat(quotation: Quotation, senderId: String, receiverId: String) {
        val file = File(quotation.filePath)
        if (!file.exists()) {
            Toast.makeText(this, getString(R.string.file_not_found), Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                file
            )

            val message = ChatMessage(
                senderId = senderId,
                receiverId = receiverId,
                message = "Quotation: ${quotation.companyName} - Status: ${quotation.status} - Total: R${"%.2f".format(quotation.subtotal)}",
                timestamp = System.currentTimeMillis(),
                attachmentUri = uri.toString(),
                quotationId = quotation.id,
                type = "quotation"
            )

            chatRef.push().setValue(message)
                .addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.quotation_sent), Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Quotation sent to admin: ${quotation.id}")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        getString(R.string.error_sending, e.message),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "Failed to send quotation: ${e.message}")
                }

        } catch (e: Exception) {
            Toast.makeText(
                this,
                getString(R.string.error_sending, e.message),
                Toast.LENGTH_SHORT
            ).show()
            Log.e(TAG, "Error sending quotation: ${e.message}")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_admin_quotations, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_refresh -> {
                refreshQuotations()
                Toast.makeText(this, getString(R.string.refreshing), Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_export -> {
                exportQuotations()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }

    private fun exportQuotations() {
        if (quotations.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_quotations), Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(
            this,
            "Exporting ${quotations.size} quotations...",
            Toast.LENGTH_SHORT
        ).show()

        // Implementation for exporting quotations would go here
        // This could generate a CSV or PDF report of all quotations
        Log.d(TAG, "Exporting ${quotations.size} quotations")
    }

    override fun onDestroy() {
        super.onDestroy()

        // Clean up Firebase listeners
        quotationsListener?.let { dbRef.removeEventListener(it) }
        chatStatusListener?.let { chatRef.removeEventListener(it) }

        Log.d(TAG, "QuotationActivity destroyed and listeners removed")
    }
}