package vcmsa.projects.fkj_consultants.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import vcmsa.projects.fkj_consultants.models.ChatMessage

class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private lateinit var chatRef: DatabaseReference
    private lateinit var currentUserId: String

    fun start(otherUserId: String) {
        currentUserId = sanitizeId(FirebaseAuth.getInstance().currentUser?.uid ?: return)
        val safeOtherId = sanitizeId(otherUserId)

        chatRef = FirebaseDatabase.getInstance()
            .getReference("chats")
            .child(chatNodeKey(currentUserId, safeOtherId))

        chatRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(ChatMessage::class.java) }
                    .sortedBy { it.timestamp }
                _messages.value = list
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun send(receiverId: String, text: String) {
        val safeReceiverId = sanitizeId(receiverId)
        val msg = ChatMessage(
            senderId = currentUserId,
            receiverId = safeReceiverId,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        val key = chatRef.push().key ?: return
        chatRef.child(key).setValue(msg)
    }

    private fun chatNodeKey(user1: String, user2: String): String {
        return if (user1 < user2) "$user1-$user2" else "$user2-$user1"
    }

    // Replace illegal Firebase characters with _
    private fun sanitizeId(id: String): String {
        return id.replace(".", "_")
            .replace("#", "_")
            .replace("$", "_")
            .replace("[", "_")
            .replace("]", "_")
    }
}
