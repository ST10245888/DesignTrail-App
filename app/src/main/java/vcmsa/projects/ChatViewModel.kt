package vcmsa.projects.fkj_consultants.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import vcmsa.projects.fkj_consultants.models.ChatMessage

class ChatViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> get() = _messages

    private var listenerRegistration: ListenerRegistration? = null

    fun start(otherUserId: String) {
        val myId = auth.currentUser?.uid ?: return
        val convoId = conversationId(myId, otherUserId)

        listenerRegistration?.remove()
        listenerRegistration = db.collection("conversations")
            .document(convoId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _messages.value = snapshot.toObjects(ChatMessage::class.java)
                }
            }
    }

    fun send(otherUserId: String, text: String) {
        val myId = auth.currentUser?.uid ?: return
        val convoId = conversationId(myId, otherUserId)

        val message = ChatMessage(
            id = db.collection("conversations").document().id,
            text = text,
            senderId = myId,
            receiverId = otherUserId,
            timestamp = System.currentTimeMillis()
        )

        db.collection("conversations")
            .document(convoId)
            .collection("messages")
            .document(message.id)
            .set(message)
    }

    private fun conversationId(user1: String, user2: String): String {
        return listOf(user1, user2).sorted().joinToString("_")
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
