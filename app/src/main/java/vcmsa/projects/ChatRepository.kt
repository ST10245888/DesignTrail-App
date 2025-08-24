package vcmsa.projects.fkj_consultants.data


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import vcmsa.projects.fkj_consultants.models.ChatMessage
import vcmsa.projects.fkj_consultants.models.Conversation

import kotlin.math.min
import kotlin.math.max

class ChatRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private fun conversationId(a: String, b: String): String {
        return listOf(a, b).sorted().joinToString("_")
    }

    suspend fun sendMessage(toUserId: String, text: String) {
        val from = auth.currentUser?.uid ?: return
        val convId = conversationId(from, toUserId)
        val msgRef = db.collection("chats").document(convId)
            .collection("messages").document()
        val message = ChatMessage(
            id = msgRef.id, text = text, senderId = from,
            receiverId = toUserId, timestamp = System.currentTimeMillis()
        )
        db.runBatch { batch ->
            batch.set(msgRef, message)
            val convo = Conversation(
                id = convId,
                userA = listOf(from, toUserId).sorted()[0],
                userB = listOf(from, toUserId).sorted()[1],
                lastMessage = text,
                lastTimestamp = message.timestamp
            )
            batch.set(db.collection("chats").document(convId), convo)
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
        val reg = db.collection("chats")
            .whereArrayContainsAny("participants", listOf(me)) // fallback if used
            .addSnapshotListener { snap, _ ->
                @Suppress("UNCHECKED_CAST")
                val docs = snap?.documents ?: emptyList()
                val convos = docs.mapNotNull { it.toObject(Conversation::class.java) }
                trySend(convos)
            }
        awaitClose { reg.remove() }
    }
}
