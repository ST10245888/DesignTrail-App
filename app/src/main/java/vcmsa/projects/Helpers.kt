package vcmsa.projects

class Helpers {
    fun buildConversationId(a: String, b: String): String {
        return if (a <= b) "${a}_$b" else "${b}_$a"
    }
}