package vcmsa.projects.fkj_consultants.activities

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.adapters.ChatAdapter
import vcmsa.projects.fkj_consultants.models.ChatMessage

class AdminChatViewActivity : AppCompatActivity() {

    private lateinit var recyclerChat: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var btnBack: ImageButton
    private lateinit var tvHeader: TextView
    private lateinit var adapter: ChatAdapter
    private lateinit var chatRef: DatabaseReference
    private lateinit var chatRoot: DatabaseReference
    private lateinit var bottomNav: BottomNavigationView

    private val auth = FirebaseAuth.getInstance()
    private val adminEmails = listOf(
        "kush@gmail.com",
        "keitumetse01@gmail.com",
        "malikaOlivia@gmail.com",
        "JamesJameson@gmail.com"
    )

    private var chatId = ""
    private var userEmail = ""
    private var userId = ""
    private var adminEmail = ""
    private var quotationId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_chat_view)
        Log.d("AdminChatView", "onCreate: Activity started")

        initializeViews()
        getIntentExtras()
        setupBottomNav()

        adminEmail = auth.currentUser?.email ?: run {
            Log.e("AdminChatView", "No authenticated admin user")
            Toast.makeText(this, "Authentication required", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupChat()
        setupRecyclerView()
        setupClickListeners()
        handleIntentAttachments()

        Log.d("AdminChatView", "Chat opened for user: $userEmail (chatId=$chatId, userId=$userId)")
    }

    private fun initializeViews() {
        try {
            recyclerChat = findViewById(R.id.recyclerAdminChat)
            etMessage = findViewById(R.id.etAdminMessage)
            btnSend = findViewById(R.id.btnAdminSend)
            btnBack = findViewById(R.id.btnBack)
            tvHeader = findViewById(R.id.tvChatHeader)
            bottomNav = findViewById(R.id.adminBottomNav)

            Log.d("AdminChatView", "initializeViews: All views initialized successfully")
            Log.d("AdminChatView", "btnBack is initialized: ${::btnBack.isInitialized}")
        } catch (e: Exception) {
            Log.e("AdminChatView", "Error initializing views: ${e.message}")
            Toast.makeText(this, "Error initializing UI", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getIntentExtras() {
        chatId = intent.getStringExtra("chatId") ?: ""
        userEmail = intent.getStringExtra("userEmail") ?: ""
        userId = intent.getStringExtra("userId") ?: ""
        quotationId = intent.getStringExtra("quotationId") ?: ""

        // If no chatId provided, generate one from userEmail
        if (chatId.isEmpty() && userEmail.isNotEmpty()) {
            chatId = encodeEmail(userEmail)
        }

        Log.d("AdminChatView", "getIntentExtras: chatId=$chatId, userEmail=$userEmail")
    }

    private fun setupChat() {
        if (chatId.isEmpty()) {
            Log.e("AdminChatView", "No chatId or userEmail provided")
            Toast.makeText(this, "Chat information missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvHeader.text = "Chat with $userEmail"

        chatRoot = FirebaseDatabase.getInstance().getReference("chats").child(chatId)
        chatRef = chatRoot.child("messages")

        initializeMetadata()
        listenForMessages()

        // Reset unread count for current admin only
        chatRoot.child("metadata").child("unreadCounts")
            .child(encodeEmail(adminEmail)).setValue(0)

        Log.d("AdminChatView", "setupChat: Chat setup completed")
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(mutableListOf(), auth.currentUser?.uid ?: "")
        recyclerChat.layoutManager = LinearLayoutManager(this)
        recyclerChat.adapter = adapter
    }

    private fun setupClickListeners() {
        Log.d("AdminChatView", "setupClickListeners: Setting up click listeners")

        // Send button
        btnSend.setOnClickListener {
            Log.d("AdminChatView", "Send button clicked")
            sendMessage()
        }

        // Back button - multiple ways to handle it
        btnBack.setOnClickListener {
            Log.d("AdminChatView", "Back button clicked")
            onBackButtonClicked()
        }

        Log.d("AdminChatView", "setupClickListeners: Click listeners set up")
    }

    private fun onBackButtonClicked() {
        Log.d("AdminChatView", "onBackButtonClicked: Finishing activity")
        Toast.makeText(this, "Returning to dashboard", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun setupBottomNav() {
        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_quotations -> {
                    navigateTo(AdminQuotationListActivity::class.java)
                    true
                }
                R.id.nav_inventory -> {
                    navigateTo(InventoryActivity::class.java)
                    true
                }
                R.id.nav_messages -> {
                    openMessagingDashboard()
                    true
                }
                else -> false
            }
        }
    }

    private fun handleIntentAttachments() {
        // Handle text quotation
        intent.getStringExtra("quotationText")?.let { textContent ->
            Log.d("AdminChatView", "Handling text quotation attachment")
            sendTextAttachmentMessage(textContent, "Quotation")
        }

        // Handle PDF quotation
        intent.getStringExtra("quotationPdfUri")?.let { pdfUri ->
            Log.d("AdminChatView", "Handling PDF quotation attachment")
            sendAttachmentMessage(Uri.parse(pdfUri), "PDF Quotation")
        }
    }

    private fun navigateTo(activityClass: Class<*>) {
        val intent = android.content.Intent(this, activityClass)
        startActivity(intent)
        finish()
    }

    private fun openMessagingDashboard() {
        val intent = android.content.Intent(this, AdminDashboardActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun encodeEmail(email: String): String {
        return email.replace(".", ",")
    }

    private fun initializeMetadata() {
        val metadataRef = chatRoot.child("metadata")
        val updates = mapOf(
            "chatId" to chatId,
            "userEmail" to userEmail,
            "userId" to userId,
            "adminEmail" to adminEmail,
            "adminEmails" to adminEmails.map { encodeEmail(it) },
            "lastMessage" to "",
            "lastTimestamp" to System.currentTimeMillis(),
            "lastSenderId" to ""
        )
        metadataRef.updateChildren(updates)

        // Initialize unread counts for all admins
        val unreadMap = mutableMapOf<String, Int>()
        adminEmails.forEach { admin ->
            unreadMap[encodeEmail(admin)] = 0
        }
        metadataRef.child("unreadCounts").setValue(unreadMap)

        Log.d("AdminChatView", "initializeMetadata: Metadata initialized")
    }

    private fun listenForMessages() {
        chatRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(ChatMessage::class.java)?.let { message ->
                    adapter.addMessage(message)
                    recyclerChat.scrollToPosition(adapter.itemCount - 1)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {
                Log.e("AdminChatView", "Database listener cancelled: ${error.message}")
            }
        })

        Log.d("AdminChatView", "listenForMessages: Message listener started")
    }

    private fun sendMessage() {
        val text = etMessage.text.toString().trim()
        if (TextUtils.isEmpty(text)) {
            Log.d("AdminChatView", "sendMessage: Empty message, not sending")
            return
        }

        val sender = auth.currentUser ?: return

        val message = ChatMessage(
            senderId = sender.uid,
            receiverId = userId,
            message = text,
            timestamp = System.currentTimeMillis(),
            quotationId = quotationId
        )

        pushMessage(message)
    }

    private fun pushMessage(message: ChatMessage) {
        chatRef.push().setValue(message)
            .addOnSuccessListener {
                etMessage.text.clear()
                Log.d("AdminChatView", "✅ Message sent successfully")
            }
            .addOnFailureListener { e ->
                Log.e("AdminChatView", "❌ Message push failed: ${e.message}")
                Toast.makeText(this@AdminChatViewActivity, "Failed to send message", Toast.LENGTH_SHORT).show()
            }

        updateMetadata(message)
    }

    private fun updateMetadata(message: ChatMessage) {
        val metadataRef = chatRoot.child("metadata")

        metadataRef.child("unreadCounts").get().addOnSuccessListener { snapshot ->
            val unreadMap = mutableMapOf<String, Any>()
            val baseMetadata = mapOf(
                "chatId" to chatId,
                "userEmail" to userEmail,
                "userId" to userId,
                "adminEmail" to adminEmail,
                "lastMessage" to message.message,
                "lastTimestamp" to message.timestamp,
                "lastSenderId" to message.senderId
            )

            adminEmails.forEach { email ->
                val encodedEmail = encodeEmail(email)
                if (email != adminEmail) {
                    val currentCount = snapshot.child(encodedEmail).getValue(Int::class.java) ?: 0
                    unreadMap[encodedEmail] = currentCount + 1
                    Log.d("AdminChatView", "📊 Incrementing unread for $email: $currentCount -> ${currentCount + 1}")
                } else {
                    unreadMap[encodedEmail] = 0
                }
            }

            val allUpdates = baseMetadata + mapOf("unreadCounts" to unreadMap)

            metadataRef.updateChildren(allUpdates)
                .addOnSuccessListener {
                    Log.d("AdminChatView", "✅ Metadata updated successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("AdminChatView", "❌ Failed to update metadata: ${e.message}")
                }
        }.addOnFailureListener { e ->
            Log.e("AdminChatView", "❌ Failed to read current unread counts: ${e.message}")
        }
    }

    private fun sendTextAttachmentMessage(textContent: String, description: String) {
        val sender = auth.currentUser ?: return

        val message = ChatMessage(
            senderId = sender.uid,
            receiverId = userId,
            message = description,
            timestamp = System.currentTimeMillis(),
            attachmentUri = textContent,
            quotationId = quotationId
        )

        pushMessage(message)
    }

    private fun sendAttachmentMessage(uri: Uri, description: String) {
        val sender = auth.currentUser ?: return

        val message = ChatMessage(
            senderId = sender.uid,
            receiverId = userId,
            message = description,
            timestamp = System.currentTimeMillis(),
            attachmentUri = uri.toString(),
            quotationId = quotationId
        )

        pushMessage(message)
    }

    // Handle device back button press
    override fun onBackPressed() {
        Log.d("AdminChatView", "onBackPressed: Device back button pressed")
        super.onBackPressed()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("AdminChatView", "onDestroy: Activity destroyed")
    }
}