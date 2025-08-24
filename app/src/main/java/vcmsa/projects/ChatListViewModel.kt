package vcmsa.projects.fkj_consultants.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import vcmsa.projects.fkj_consultants.models.Conversation

class ChatListViewModel : ViewModel() {

    private val _conversations = MutableLiveData<List<Conversation>>()
    val conversations: LiveData<List<Conversation>> get() = _conversations

    // Firebase user ID
    val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    fun start() {
        // TODO: Replace with Firestore/Realtime DB listener
        // Example (dummy):
        _conversations.value = listOf(
            Conversation("user1_user2", "Hey, how are you?", System.currentTimeMillis().toString()),
            Conversation("user1_user3", "Let’s meet tomorrow",
                System.currentTimeMillis().toString()
            )
        )
    }
}
