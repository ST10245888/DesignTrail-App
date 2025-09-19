package vcmsa.projects.fkj_consultants.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import vcmsa.projects.fkj_consultants.data.ChatRepository
import vcmsa.projects.fkj_consultants.models.ChatMessage

class ChatViewModel(private val repo: ChatRepository = ChatRepository()) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    fun start(otherUserId: String) {
        viewModelScope.launch {
            repo.observeMessages(otherUserId).collect { list ->
                _messages.value = list
            }
        }
    }

    fun send(otherUserId: String, text: String) {
        viewModelScope.launch {
            repo.sendMessage(otherUserId, text)
        }
    }

    /** Returns current user UID for adapter alignment */
    fun getCurrentUserId(): String = repo.auth.currentUser?.uid ?: ""
}
