package vcmsa.projects.fkj_consultants.models

data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val timestamp: Long = 0L
)