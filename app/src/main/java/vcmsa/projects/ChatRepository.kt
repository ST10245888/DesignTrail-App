package vcmsa.projects.fkj_consultants.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import vcmsa.projects.fkj_consultants.models.ChatMessage
import vcmsa.projects.fkj_consultants.models.Conversation

class ChatRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private fun conversationId(a: String, b: String): String =
        listOf(a, b).sorted().joinToString("_")

    suspend fun sendMessage(toUserId: String, text: String) {
        val from = auth.currentUser?.uid ?: return
        val convId = conversationId(from, toUserId)

        // Message
        val msgRef = db.collection("chats").document(convId)
            .collection("messages").document()
        val message = ChatMessage(
            id = msgRef.id,
            text = text,
            senderId = from,
            receiverId = toUserId,
            timestamp = System.currentTimeMillis()
        )

        // Conversation info
        val convo = Conversation(
            id = convId,
            userA = listOf(from, toUserId).sorted()[0],
            userB = listOf(from, toUserId).sorted()[1],
            lastMessage = text,
            lastTimestamp = message.timestamp
        )

        // Batch write
        db.runBatch { batch ->
            batch.set(msgRef, message)
            batch.set(db.collection("conversations").document(convId), convo)
        }.await()
    }

    fun observeMessages(otherUserId: String) = callbackFlow<List<ChatMessage>> {
        val me = auth.currentUser?.uid ?: run { trySend(emptyList()); close(); return@callbackFlow }
        val convId = conversationId(me, otherUserId)
        val reg = db.collection("chats").document(convId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                val list = snap?.toObjects(ChatMessage::class.java) ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    fun observeConversations() = callbackFlow<List<Conversation>> {
        val me = auth.currentUser?.uid ?: run { trySend(emptyList()); close(); return@callbackFlow }
        val reg = db.collection("conversations")
            .addSnapshotListener { snap, _ ->
                val list = snap?.toObjects(Conversation::class.java)?.sortedByDescending { it.lastTimestamp } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }
}
