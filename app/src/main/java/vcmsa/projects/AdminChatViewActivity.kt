package vcmsa.projects.fkj_consultants.activities

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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
    private lateinit var tvHeader: TextView
    private lateinit var adapter: ChatAdapter
    private lateinit var chatRef: DatabaseReference
    private lateinit var chatRoot: DatabaseReference

    private val auth = FirebaseAuth.getInstance()
    private val adminEmails = listOf(
        "kush@gmail.com",
        "keitumetse01@gmail.com",
        "malikaOlivia@gmail.com",
        "JamesJameson@gmail.com"
    )

    private lateinit var chatId: String
    private lateinit var userEmail: String
    private lateinit var adminEmail: String
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_chat_view)

        recyclerChat = findViewById(R.id.recyclerAdminChat)
        etMessage = findViewById(R.id.etAdminMessage)
        btnSend = findViewById(R.id.btnAdminSend)
        tvHeader = findViewById(R.id.tvChatHeader)

        bottomNav = findViewById(R.id.adminBottomNav)
        setupBottomNav()
        adminEmail = auth.currentUser?.email ?: run { finish(); return }
        userEmail = intent.getStringExtra("userEmail") ?: run { finish(); return }

        tvHeader.text = "Chat with $userEmail"

        // One chat per user for all admins
        chatId = encodeEmail(userEmail)
        chatRoot = FirebaseDatabase.getInstance().getReference("chats").child(chatId)
        chatRef = chatRoot.child("messages")

        // Initialize metadata (ensures admin dashboard sees the user)
        initializeMetadata()

        adapter = ChatAdapter(mutableListOf(), adminEmail)
        recyclerChat.layoutManager = LinearLayoutManager(this)
        recyclerChat.adapter = adapter

        listenForMessages()

        // Reset unread count for this admin only
        chatRoot.child("metadata").child("unreadCounts")
            .child(encodeEmail(adminEmail)).setValue(0)

        btnSend.setOnClickListener { sendMessage() }

        // Handle optional quotation text attachment
        intent.getStringExtra("quotationText")?.let { textContent ->
            sendTextAttachmentMessage(textContent, "Quotation")
        }

        // Handle optional PDF or file attachment
        intent.getStringExtra("quotationPdfUri")?.let { pdfUri ->
            sendAttachmentMessage(Uri.parse(pdfUri), "PDF Quotation")
        }

        Log.d("AdminChatView", "Chat opened for $userEmail (chatId=$chatId)")
    }

    /** Generic navigation helper */
    private fun navigateTo(activityClass: Class<*>) {
        val intent = android.content.Intent(this, activityClass)
        startActivity(intent)
    }

    /** Open admin messaging dashboard (replace with your actual dashboard activity) */
    private fun openMessagingDashboard() {
        // Replace AdminDashboardActivity with your actual messaging dashboard activity
        val intent = android.content.Intent(this, AdminDashboardActivity::class.java)
        startActivity(intent)
    }

    private fun setupBottomNav() {
        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_quotations -> { navigateTo(AdminQuotationListActivity::class.java); true }
                R.id.nav_inventory -> { navigateTo(InventoryActivity::class.java); true }
                R.id.nav_messages -> { openMessagingDashboard(); true }
                else -> false
            }
        }
    }
    private fun encodeEmail(email: String) = email.replace(".", ",")

    /** Initialize metadata to ensure dashboard shows this chat */
    private fun initializeMetadata() {
        val metadataRef = chatRoot.child("metadata")
        metadataRef.child("chatId").setValue(chatId)
        metadataRef.child("userEmail").setValue(userEmail)
        metadataRef.child("adminEmails").setValue(adminEmails.map { encodeEmail(it) })
        metadataRef.child("lastMessage").setValue("")
        metadataRef.child("lastTimestamp").setValue(System.currentTimeMillis())
        metadataRef.child("lastSenderId").setValue("")
        val unreadMap = mutableMapOf<String, Int>()
        adminEmails.forEach { admin -> unreadMap[encodeEmail(admin)] = 0 }
        metadataRef.child("unreadCounts").setValue(unreadMap)
    }

    private fun listenForMessages() {
        chatRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { it.getValue(ChatMessage::class.java) }
                    .sortedBy { it.timestamp }
                adapter.setMessages(messages)
                if (adapter.itemCount > 0) {
                    recyclerChat.scrollToPosition(adapter.itemCount - 1)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AdminChatView", "listenForMessages cancelled: ${error.message}")
            }
        })
    }

    private fun sendMessage() {
        val text = etMessage.text.toString().trim()
        if (TextUtils.isEmpty(text)) return

        val timestamp = System.currentTimeMillis()
        val message = ChatMessage(
            senderId = adminEmail,
            receiverId = userEmail,
            message = text,
            timestamp = timestamp
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
            }

        // Update metadata - read current counts first, then increment for other admins
        val metadataRef = chatRoot.child("metadata")
        metadataRef.child("unreadCounts").get().addOnSuccessListener { snapshot ->
            val unreadMap = mutableMapOf<String, Any>()

            adminEmails.forEach { email ->
                val encodedEmail = encodeEmail(email)
                if (email != adminEmail) {
                    // Increment unread count for other admins
                    val currentCount = snapshot.child(encodedEmail).getValue(Int::class.java) ?: 0
                    unreadMap[encodedEmail] = currentCount + 1
                    Log.d("AdminChatView", "📊 Incrementing unread for $email: $currentCount -> ${currentCount + 1}")
                } else {
                    // Keep current admin's count at 0
                    unreadMap[encodedEmail] = 0
                }
            }

            val metadata = mapOf(
                "chatId" to chatId,
                "userEmail" to userEmail,
                "adminEmails" to adminEmails.map { encodeEmail(it) },
                "lastMessage" to message.message,
                "lastTimestamp" to message.timestamp,
                "lastSenderId" to message.senderId,
                "unreadCounts" to unreadMap
            )

            metadataRef.updateChildren(metadata)
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



    /** Send text-based attachment like a quotation */
    private fun sendTextAttachmentMessage(textContent: String, description: String) {
        val message = ChatMessage(
            senderId = adminEmail,
            receiverId = userEmail,
            message = description,
            timestamp = System.currentTimeMillis(),
            attachmentUri = textContent // store text content directly
        )
        pushMessage(message)
    }

    /** Send file-based attachment (PDF, image, etc.) */
    private fun sendAttachmentMessage(uri: Uri, description: String) {
        val message = ChatMessage(
            senderId = adminEmail,
            receiverId = userEmail,
            message = description,
            timestamp = System.currentTimeMillis(),
            attachmentUri = uri.toString()
        )
        pushMessage(message)
    }
}
// (Android Developers, 2025).