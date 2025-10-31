package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import vcmsa.projects.fkj_consultants.adapters.ChatThreadAdapter
import vcmsa.projects.fkj_consultants.models.ChatThread
import vcmsa.projects.fkj_consultants.R

class AdminChatDashboardActivity : AppCompatActivity() {

    private lateinit var recyclerThreads: RecyclerView
    private lateinit var adapter: ChatThreadAdapter
    private lateinit var dbRef: DatabaseReference
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "AdminChatDashboard"
    private lateinit var adminEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_chat_dashboard)

        val admin = auth.currentUser ?: run { finish(); return }
        adminEmail = admin.email ?: "admin@default.com"

        recyclerThreads = findViewById(R.id.recyclerThreads)
        recyclerThreads.layoutManager = LinearLayoutManager(this)

        adapter = ChatThreadAdapter(mutableListOf()) { thread ->
            val intent = Intent(this, AdminChatViewActivity::class.java)
            intent.putExtra("chatId", thread.chatId)
            intent.putExtra("userEmail", thread.userEmail)
            intent.putExtra("userId", thread.userId)
            startActivity(intent)
        }
        recyclerThreads.adapter = adapter

        dbRef = FirebaseDatabase.getInstance().getReference("chats")
        listenForThreads()
    }

    private fun encodeEmail(email: String) = email.replace(".", ",")

    private fun listenForThreads() {
        // Listen for all chats dynamically
        dbRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                handleThreadSnapshot(snapshot)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                handleThreadSnapshot(snapshot)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                snapshot.key?.let { chatId ->
                    adapter.removeThread(chatId)
                    Log.d(TAG, "Thread removed: chatId=$chatId")
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Firebase error: ${error.message}", error.toException())
            }
        })
    }

    private fun handleThreadSnapshot(snapshot: DataSnapshot) {
        val metadataSnap = snapshot.child("metadata")
        if (!metadataSnap.exists()) return

        val chatId = snapshot.key ?: return
        val userEmail = metadataSnap.child("userEmail").getValue(String::class.java) ?: return
        val userId = metadataSnap.child("userId").getValue(String::class.java) ?: encodeEmail(userEmail)
        val lastMessage = metadataSnap.child("lastMessage").getValue(String::class.java) ?: ""
        val lastTimestamp = metadataSnap.child("lastTimestamp").getValue(Long::class.java) ?: 0L

        // Get this admin's unread count
        val unreadCountsSnap = metadataSnap.child("unreadCounts")
        val unreadCount = unreadCountsSnap.child(encodeEmail(adminEmail)).getValue(Int::class.java) ?: 0

        val thread = ChatThread(
            chatId = chatId,
            userEmail = userEmail,
            adminEmail = adminEmail,
            lastMessage = lastMessage,
            lastTimestamp = lastTimestamp,
            userId = userId,
            unreadCount = unreadCount
        )

        adapter.addOrUpdateThread(thread)

        // Keep threads sorted by lastTimestamp descending
        adapter.sortByTimestampDesc()

        Log.d(TAG, "Thread loaded/updated: chatId=$chatId, user=$userEmail, lastMessage=$lastMessage, unread=$unreadCount")
    }
}
