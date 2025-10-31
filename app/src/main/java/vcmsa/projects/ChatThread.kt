package vcmsa.projects.fkj_consultants.models

data class ChatThread(
    val chatId: String,
    val userEmail: String,
    val adminEmail: String,
    val lastMessage: String,
    val lastTimestamp: Long,
    val userId: String,
    val unreadCount: Int = 0
)
