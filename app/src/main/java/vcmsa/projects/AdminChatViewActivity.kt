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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.adapters.ChatAdapter
import vcmsa.projects.fkj_consultants.models.ChatMessage
import vcmsa.projects.fkj_consultants.utils.ChatUtils

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_chat_view)

        recyclerChat = findViewById(R.id.recyclerAdminChat)
        etMessage = findViewById(R.id.etAdminMessage)
        btnSend = findViewById(R.id.btnAdminSend)
        tvHeader = findViewById(R.id.tvChatHeader)

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
                recyclerChat.scrollToPosition(adapter.itemCount - 1)
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
            .addOnSuccessListener { etMessage.text.clear() }
            .addOnFailureListener { e -> Log.e("AdminChatView", "Message push failed: ${e.message}") }

        // Update metadata for all admins
        val unreadMap = mutableMapOf<String, Int>()
        adminEmails.forEach { email ->
            if (email != adminEmail) {
                unreadMap[encodeEmail(email)] = ((unreadMap[encodeEmail(email)] ?: 0) + 1)
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
        chatRoot.child("metadata").updateChildren(metadata)
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
