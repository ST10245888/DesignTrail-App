package vcmsa.projects.fkj_consultants.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

/**
 * ChatThread - Represents a chat conversation thread between user and admin(s)
 */
@Parcelize
data class ChatThread(
    // Core Identification
    val chatId: String = "",                    // Unique chat identifier (encoded user email)
    val userEmail: String = "",                 // User's email address
    val adminEmail: String = "",                // Primary admin email
    val userId: String = "",                    // User ID for reference

    // Chat Metadata
    val lastMessage: String = "",               // Last message content
    val lastTimestamp: Long = 0L,               // Last message timestamp (CHANGED TO Long)
    val lastSenderId: String = "",              // Last sender's ID

    // Status & Notifications
    val unreadCount: Int = 0,                   // Number of unread messages
    val isActive: Boolean = true,               // Whether chat is active
    val isArchived: Boolean = false,            // Whether chat is archived

    // Additional Metadata
    val createdAt: Long = System.currentTimeMillis(), // Thread creation timestamp
    val updatedAt: Long = System.currentTimeMillis(), // Last update timestamp
    val adminEmails: List<String> = emptyList(), // All admin emails in this chat
    val metadata: Map<String, String> = emptyMap() // Additional data
) : Parcelable {

    // Computed Properties
    val hasUnreadMessages: Boolean
        get() = unreadCount > 0

    val formattedLastTimestamp: String
        get() = formatTimestamp(lastTimestamp)

    val isUserOnline: Boolean
        get() = false // You can implement online status logic here

    val displayName: String
        get() = userEmail.substringBefore("@")

    companion object {
        /**
         * Creates a ChatThread from user email
         */
        fun fromUserEmail(
            userEmail: String,
            adminEmail: String = "",
            lastMessage: String = "",
            lastTimestamp: Long = System.currentTimeMillis()
        ): ChatThread {
            return ChatThread(
                chatId = encodeEmail(userEmail),
                userEmail = userEmail,
                adminEmail = adminEmail,
                userId = userEmail, // Using email as ID for simplicity
                lastMessage = lastMessage,
                lastTimestamp = lastTimestamp,
                lastSenderId = userEmail
            )
        }

        /**
         * Creates a ChatThread for admin dashboard
         */
        fun forAdmin(
            userEmail: String,
            adminEmail: String,
            lastMessage: String = "",
            lastTimestamp: Long = System.currentTimeMillis(),
            unreadCount: Int = 0
        ): ChatThread {
            return ChatThread(
                chatId = encodeEmail(userEmail),
                userEmail = userEmail,
                adminEmail = adminEmail,
                userId = userEmail,
                lastMessage = lastMessage,
                lastTimestamp = lastTimestamp,
                lastSenderId = userEmail,
                unreadCount = unreadCount
            )
        }

        /**
         * Encodes email for Firebase compatibility
         */
        fun encodeEmail(email: String): String = email.replace(".", ",")

        /**
         * Formats timestamp for display
         */
        private fun formatTimestamp(timestamp: Long): String {
            return try {
                val date = Date(timestamp)
                val format = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                format.format(date)
            } catch (e: Exception) {
                "Unknown time"
            }
        }
    }

    /**
     * Updates thread with new message
     */
    fun updateWithMessage(message: ChatMessage, isAdminViewing: Boolean = false): ChatThread {
        return this.copy(
            lastMessage = message.message,
            lastTimestamp = message.timestamp,
            lastSenderId = message.senderId,
            unreadCount = if (isAdminViewing) 0 else unreadCount + 1,
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Marks all messages as read
     */
    fun markAsRead(): ChatThread {
        return this.copy(
            unreadCount = 0,
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Increments unread count
     */
    fun incrementUnreadCount(): ChatThread {
        return this.copy(
            unreadCount = unreadCount + 1,
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Archives the chat thread
     */
    fun archive(): ChatThread {
        return this.copy(
            isArchived = true,
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Activates the chat thread
     */
    fun activate(): ChatThread {
        return this.copy(
            isActive = true,
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Checks if thread belongs to specific user
     */
    fun belongsToUser(email: String): Boolean {
        return userEmail.equals(email, ignoreCase = true)
    }

    /**
     * Checks if thread involves specific admin
     */
    fun involvesAdmin(email: String): Boolean {
        return adminEmail.equals(email, ignoreCase = true) ||
                adminEmails.any { it.equals(email, ignoreCase = true) }
    }

    /**
     * Validates thread data
     */
    fun isValid(): Boolean {
        return chatId.isNotBlank() &&
                userEmail.isNotBlank() &&
                userId.isNotBlank() &&
                lastTimestamp > 0
    }
}