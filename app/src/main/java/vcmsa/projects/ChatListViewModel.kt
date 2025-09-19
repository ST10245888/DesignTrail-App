package vcmsa.projects.fkj_consultants.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import vcmsa.projects.fkj_consultants.models.Conversation

class ChatListViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _conversations = MutableLiveData<List<Conversation>>()
    val conversations: LiveData<List<Conversation>> get() = _conversations

    fun startAdmin() {
        // Fetch all conversations for admin view
        db.collection("chats")
            .orderBy("lastTimestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _conversations.value = emptyList()
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(Conversation::class.java) ?: emptyList()
                _conversations.value = list
            }
    }
}
