package vcmsa.projects.fkj_consultants.models

data class Conversation(
    val id: String = "",
    val userA: String = "",
    val userB: String = "",
    val lastMessage: String = "",
    val lastTimestamp: Long = 0L
) {
    fun getOtherUserId(myId: String): String {
        return if (userA != myId) userA else userB
    }
}