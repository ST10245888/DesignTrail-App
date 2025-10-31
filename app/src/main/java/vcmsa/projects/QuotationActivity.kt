package vcmsa.projects.fkj_consultants.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quotation)

        // 🔙 Back button setup
        val btnBack: ImageButton = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            Log.d(TAG, "Back button clicked — finishing activity")
            finish()
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        recyclerView = findViewById(R.id.recyclerQuotations)
        recyclerView.layoutManager = LinearLayoutManager(this)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        currentUserId = user.uid

        chatId = if (currentUserId < receiverId) "${currentUserId}_$receiverId"
        else "${receiverId}_$currentUserId"
        chatRoot = FirebaseDatabase.getInstance().getReference("chats").child(chatId)
        chatRef = chatRoot.child("messages")

        dbRef = FirebaseDatabase.getInstance().getReference("quotations")

        adapter = QuotationAdapter(
            quotations,
            currentUserId,
            receiverId,
            onSendClick = ::sendQuotationToChat
        )
        recyclerView.adapter = adapter

        fetchUserQuotationsRealtime()
    }

    /** Fetch all quotations for the current user and update status in real-time **/
    private fun fetchUserQuotationsRealtime() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updatedList = mutableListOf<Quotation>()
                snapshot.children.forEach { snap ->
                    val quotation = snap.getValue(Quotation::class.java)
                    if (quotation != null && quotation.userId == currentUserId) {
                        // Make sure id is set
                        if (quotation.id.isEmpty()) quotation.id = snap.key ?: ""
                        updatedList.add(quotation)
                    }
                }
                quotations.clear()
                quotations.addAll(updatedList)
                adapter.updateQuotations(updatedList)
                Log.d(TAG, "User quotations updated: ${updatedList.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to fetch quotations: ${error.message}")
                Toast.makeText(this@QuotationActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /** Sends quotation to chat (admin) **/
    @SuppressLint("RestrictedApi")
    private fun sendQuotationToChat(quotation: Quotation, senderId: String, receiverId: String) {
        val file = File(quotation.filePath)
        if (!file.exists()) {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                file
            )

            val message = ChatMessage(
                senderId = senderId,
                receiverId = receiverId,
                message = "Quotation: ${quotation.companyName}",
                timestamp = System.currentTimeMillis(),
                attachmentUri = uri.toString()
            )

            chatRef.push().setValue(message)
                .addOnSuccessListener {
                    Toast.makeText(this, "Quotation sent to chat", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to send: ${e.message}", Toast.LENGTH_SHORT).show()
                }

        } catch (e: Exception) {
            Toast.makeText(this, "Error sending quotation: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
// (Android Developers, 2025).