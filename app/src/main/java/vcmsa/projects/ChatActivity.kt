package vcmsa.projects.fkj_consultants.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.adapters.ChatMessageAdapter
import vcmsa.projects.fkj_consultants.models.ChatMessage
import java.io.File
import java.io.FileOutputStream

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerChat: RecyclerView
    private lateinit var etMessage: androidx.appcompat.widget.AppCompatEditText
    private lateinit var btnSend: androidx.appcompat.widget.AppCompatButton
    private lateinit var btnAttach: ImageButton
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var adapter: ChatMessageAdapter
    private lateinit var chatRef: DatabaseReference
    private lateinit var chatRoot: DatabaseReference

    private val auth = FirebaseAuth.getInstance()
    private lateinit var currentUserEmail: String
    private val adminEmails = listOf(
        "kush@gmail.com",
        "keitumetse01@gmail.com",
        "malikaOlivia@gmail.com",
        "JamesJameson@gmail.com"
    )
    private var isAdmin = false
    private lateinit var chatId: String
    private lateinit var userEmail: String

    // File picker launcher
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleAttachmentSelection(uri)
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerChat = findViewById(R.id.recyclerChat)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        btnAttach = findViewById(R.id.btnAttach)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        val user = auth.currentUser ?: run { finish(); return }
        currentUserEmail = user.email ?: "unknown@user.com"
        isAdmin = adminEmails.contains(currentUserEmail)

        // Determine userEmail for this chat
        userEmail = if (isAdmin) {
            intent.getStringExtra("targetUserEmail") ?: run { finish(); return }
        } else {
            currentUserEmail
        }

        // One chat per user; encode email for Firebase safety
        chatId = encodeEmail(userEmail)
        chatRoot = FirebaseDatabase.getInstance().getReference("chats").child(chatId)
        chatRef = chatRoot.child("messages")

        // Initialize metadata for the user (ensures Admin sees them in dashboard)
        initializeMetadata()

        adapter = ChatMessageAdapter(mutableListOf(), currentUserEmail)
        recyclerChat.layoutManager = LinearLayoutManager(this)
        recyclerChat.adapter = adapter

        listenForMessages()

        btnSend.setOnClickListener { sendMessage() }
        btnAttach.setOnClickListener { openFilePicker() }

        setupBottomNavigation()

        Log.d("ChatActivity", "Chat started for userEmail=$userEmail (Admin: $isAdmin)")
    }

    private fun encodeEmail(email: String): String = email.replace(".", ",")

    private fun initializeMetadata() {
        val metadataRef = chatRoot.child("metadata")
        metadataRef.child("chatId").setValue(chatId)
        metadataRef.child("userEmail").setValue(userEmail)
        metadataRef.child("adminEmails").setValue(adminEmails.map { encodeEmail(it) })
        metadataRef.child("lastMessage").setValue("")
        metadataRef.child("lastTimestamp").setValue(System.currentTimeMillis())
        val unreadMap = mutableMapOf<String, Int>()
        adminEmails.forEach { admin -> unreadMap[encodeEmail(admin)] = 0 }
        metadataRef.child("unreadCounts").setValue(unreadMap)
    }

    private fun listenForMessages() {
        chatRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(ChatMessage::class.java)?.let {
                    adapter.addMessage(it)
                    recyclerChat.scrollToPosition(adapter.itemCount - 1)
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatActivity", "Message listener cancelled: ${error.message}")
            }
        })
    }

    /** Open file picker for attachments */
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "application/pdf",
                "text/plain",
                "image/*",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            ))
        }
        filePickerLauncher.launch(Intent.createChooser(intent, "Select File"))
    }

    /** Handle selected file attachment */
    private fun handleAttachmentSelection(uri: Uri) {
        try {
            val fileName = getFileName(uri)
            val file = copyFileToAppStorage(uri, fileName)

            if (file != null && file.exists()) {
                Toast.makeText(this, "File attached: $fileName", Toast.LENGTH_SHORT).show()
                sendMessage(file.absolutePath)
            } else {
                Toast.makeText(this, "Failed to attach file", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("ChatActivity", "Error handling attachment: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /** Get file name from URI */
    private fun getFileName(uri: Uri): String {
        var fileName = "attachment_${System.currentTimeMillis()}"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                fileName = cursor.getString(nameIndex)
            }
        }
        return fileName
    }

    /** Copy file from URI to app storage */
    private fun copyFileToAppStorage(uri: Uri, fileName: String): File? {
        return try {
            val attachmentDir = File(getExternalFilesDir(null), "chat_attachments")
            if (!attachmentDir.exists()) attachmentDir.mkdirs()

            val destFile = File(attachmentDir, fileName)

            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            Log.d("ChatActivity", "File copied to: ${destFile.absolutePath}")
            destFile
        } catch (e: Exception) {
            Log.e("ChatActivity", "Error copying file: ${e.message}", e)
            null
        }
    }

    /**
     * Send message with optional attachment.
     * @param attachmentPath Path to local file attachment (optional)
     */
    private fun sendMessage(attachmentPath: String? = null) {
        val text = etMessage.text.toString().trim()

        // Must have either text or attachment
        if (TextUtils.isEmpty(text) && attachmentPath.isNullOrEmpty()) {
            Toast.makeText(this, "Please enter a message or attach a file", Toast.LENGTH_SHORT).show()
            return
        }

        val timestamp = System.currentTimeMillis()

        // Create message with attachment
        val message = ChatMessage(
            senderId = currentUserEmail,
            receiverId = if (isAdmin) userEmail else "admins",
            message = if (attachmentPath != null && text.isEmpty()) {
                "📎 Sent an attachment"
            } else {
                text
            },
            timestamp = timestamp,
            attachmentUri = attachmentPath
        )

        Log.d("ChatActivity", "Sending message: text='${message.message}', attachment=$attachmentPath")

        // Push message to Firebase
        chatRef.push().setValue(message)
            .addOnSuccessListener {
                etMessage.text?.clear()
                recyclerChat.scrollToPosition(adapter.itemCount - 1)
                Log.d("ChatActivity", "Message sent successfully")
                Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("ChatActivity", "Failed to send message: ${e.message}", e)
                Toast.makeText(this, "Failed to send: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        // Update metadata for admins
        updateChatMetadata(message.message, timestamp)
    }

    /** Update chat metadata */
    private fun updateChatMetadata(lastMessage: String, timestamp: Long) {
        val unreadCounts = mutableMapOf<String, Any>()
        adminEmails.forEach { email ->
            if (email != currentUserEmail) {
                // Increment unread for other admins
                unreadCounts[encodeEmail(email)] = 1
            } else {
                // Keep current admin's count at 0
                unreadCounts[encodeEmail(email)] = 0
            }
        }

        val metadata = mapOf(
            "chatId" to chatId,
            "userEmail" to userEmail,
            "adminEmails" to adminEmails.map { encodeEmail(it) },
            "lastMessage" to lastMessage,
            "lastTimestamp" to timestamp,
            "lastSenderId" to currentUserEmail,
            "unreadCounts" to unreadCounts
        )

        chatRoot.child("metadata").updateChildren(metadata)
            .addOnSuccessListener {
                Log.d("ChatActivity", "Metadata updated")
            }
            .addOnFailureListener { e ->
                Log.e("ChatActivity", "Failed to update metadata: ${e.message}", e)
            }
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> true
                R.id.nav_catalog -> {
                    startActivity(Intent(this, CatalogActivity::class.java))
                    true
                }
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, UserDashboardActivity::class.java))
                    true
                }
                R.id.nav_messages -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileAccountActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Public method to send attachments (called from other activities)
     */
    fun sendQuotationFile(file: File) {
        if (file.exists()) {
            sendMessage(file.absolutePath)
        } else {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
        }
    }
}