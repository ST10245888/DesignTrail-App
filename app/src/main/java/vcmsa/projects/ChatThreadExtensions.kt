package vcmsa.projects.fkj_consultants.extensions

import vcmsa.projects.fkj_consultants.models.ChatThread

// Extension functions for List<ChatThread>
fun List<ChatThread>.getUnreadCount(): Int {
    return this.sumOf { it.unreadCount }
}

fun List<ChatThread>.getActiveThreads(): List<ChatThread> {
    return this.filter { it.isActive && !it.isArchived }
}

fun List<ChatThread>.getArchivedThreads(): List<ChatThread> {
    return this.filter { it.isArchived }
}

fun List<ChatThread>.getThreadsWithUnread(): List<ChatThread> {
    return this.filter { it.hasUnreadMessages }
}

fun List<ChatThread>.getThreadByUserEmail(email: String): ChatThread? {
    return this.find { it.userEmail.equals(email, ignoreCase = true) }
}

fun List<ChatThread>.getThreadByChatId(chatId: String): ChatThread? {
    return this.find { it.chatId == chatId }
}

fun List<ChatThread>.sortByLastMessage(): List<ChatThread> {
    return this.sortedByDescending { it.lastTimestamp }
}

fun List<ChatThread>.sortByUnreadCount(): List<ChatThread> {
    return this.sortedByDescending { it.unreadCount }
}