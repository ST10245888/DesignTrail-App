package vcmsa.projects.fkj_consultants.models

data class ChatMessage(
    var senderId: String = "",
    var receiverId: String = "",
    var message: String = "",
    var timestamp: Long = 0L,
    var attachmentUri: String? = null
)
