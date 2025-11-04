package vcmsa.projects.fkj_consultants.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
data class ChatMessage(
    // Core Identification
    var id: String = "",                    // Unique message identifier
    var senderId: String = "",              // Sender's user ID or email
    var receiverId: String = "",            // Receiver's user ID or email
    var message: String = "",               // Message content
    var timestamp: Long = System.currentTimeMillis(), // Message creation timestamp

    // Message Metadata
    var type: String = TYPE_TEXT,           // Message type (text, quotation, file, etc.)
    var status: String = STATUS_SENT,       // Delivery status
    var read: Boolean = false,              // Read status
    var adminId: String? = null,            // Admin ID for admin-specific messages

    // Attachment Information
    var attachmentUri: String? = null,      // URI for attached files
    var fileName: String? = null,           // Original file name
    var fileSize: Long = 0L,                // File size in bytes
    var mimeType: String? = null,           // MIME type of attachment

    // Quotation Integration
    var quotationId: String = "",           // Associated quotation ID
    var quotationStatus: String? = null,    // Quotation status update

    // Additional Metadata
    var chatId: String = "",                // Chat session ID
    var replyToId: String? = null,          // Message ID this message replies to
    var edited: Boolean = false,            // Whether message has been edited
    var editedAt: Long? = null,             // Timestamp of last edit
    var deleted: Boolean = false,           // Soft delete flag
    var metadata: Map<String, String> = emptyMap() // Additional flexible metadata (changed to String for Parcelize)
) : Parcelable {

    // Computed Properties
    val isTextMessage: Boolean
        get() = type == TYPE_TEXT

    val isQuotationMessage: Boolean
        get() = type == TYPE_QUOTATION

    val isStatusUpdate: Boolean
        get() = type == TYPE_STATUS_UPDATE

    val isFileMessage: Boolean
        get() = type == TYPE_FILE

    val isImageMessage: Boolean
        get() = type == TYPE_IMAGE

    val isSystemMessage: Boolean
        get() = type == TYPE_SYSTEM

    val isSentByAdmin: Boolean
        get() = senderId == "admin" || ADMIN_EMAILS.any { it.equals(senderId, ignoreCase = true) }

    val hasAttachment: Boolean
        get() = !attachmentUri.isNullOrEmpty()

    val isQuotationStatusUpdate: Boolean
        get() = isStatusUpdate && quotationId.isNotEmpty()

    val isRead: Boolean
        get() = read

    val isDelivered: Boolean
        get() = status == STATUS_DELIVERED

    val isFailed: Boolean
        get() = status == STATUS_FAILED

    val isSending: Boolean
        get() = status == STATUS_SENDING

    val formattedTimestamp: String
        get() {
            val date = Date(timestamp)
            val format = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            return format.format(date)
        }

    val formattedDate: String
        get() {
            val date = Date(timestamp)
            val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return format.format(date)
        }

    val isToday: Boolean
        get() {
            val today = Calendar.getInstance()
            val messageDate = Calendar.getInstance().apply { time = Date(timestamp) }
            return today.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == messageDate.get(Calendar.DAY_OF_YEAR)
        }

    val fileSizeFormatted: String
        get() = when {
            fileSize <= 0 -> "Unknown size"
            fileSize < 1024 -> "$fileSize B"
            fileSize < 1024 * 1024 -> "%.1f KB".format(fileSize / 1024.0)
            else -> "%.1f MB".format(fileSize / (1024.0 * 1024.0))
        }

    companion object {
        // Message types
        const val TYPE_TEXT = "text"
        const val TYPE_QUOTATION = "quotation"
        const val TYPE_STATUS_UPDATE = "status_update"
        const val TYPE_FILE = "file"
        const val TYPE_IMAGE = "image"
        const val TYPE_SYSTEM = "system"

        // Message status
        const val STATUS_SENDING = "sending"
        const val STATUS_SENT = "sent"
        const val STATUS_DELIVERED = "delivered"
        const val STATUS_READ = "read"
        const val STATUS_FAILED = "failed"

        // Quotation status update types
        const val STATUS_UPDATE_APPROVED = "approved"
        const val STATUS_UPDATE_REJECTED = "rejected"
        const val STATUS_UPDATE_PENDING = "pending"
        const val STATUS_UPDATE_COMPLETED = "completed"

        // Admin emails for detection
        val ADMIN_EMAILS = listOf(
            "kush@gmail.com",
            "keitumetse01@gmail.com",
            "malikaOlivia@gmail.com",
            "JamesJameson@gmail.com"
        )

        /**
         * Creates a simple text message
         */
        fun createTextMessage(
            senderId: String,
            receiverId: String,
            message: String,
            chatId: String = ""
        ): ChatMessage {
            return ChatMessage(
                senderId = senderId,
                receiverId = receiverId,
                message = message,
                chatId = chatId,
                type = TYPE_TEXT,
                timestamp = System.currentTimeMillis()
            )
        }

        /**
         * Creates a text message with sending status
         */
        fun createSendingTextMessage(
            senderId: String,
            receiverId: String,
            message: String,
            chatId: String = ""
        ): ChatMessage {
            return ChatMessage(
                senderId = senderId,
                receiverId = receiverId,
                message = message,
                chatId = chatId,
                type = TYPE_TEXT,
                status = STATUS_SENDING,
                timestamp = System.currentTimeMillis()
            )
        }

        /**
         * Creates a quotation message with attachment
         */
        fun createQuotationMessage(
            senderId: String,
            receiverId: String,
            quotation: Quotation,
            fileUri: String? = null,
            chatId: String = ""
        ): ChatMessage {
            val file = File(quotation.filePath)
            return ChatMessage(
                senderId = senderId,
                receiverId = receiverId,
                message = "📄 Quotation: ${quotation.companyName} - Status: ${quotation.status} - Total: R${"%.2f".format(quotation.subtotal)}",
                timestamp = System.currentTimeMillis(),
                attachmentUri = fileUri,
                quotationId = quotation.id,
                type = TYPE_QUOTATION,
                fileName = quotation.fileName,
                fileSize = if (file.exists()) file.length() else 0L,
                mimeType = "application/pdf",
                chatId = chatId
            )
        }

        /**
         * Creates a status update message for quotations
         */
        fun createStatusUpdateMessage(
            quotation: Quotation,
            newStatus: String,
            adminId: String? = null,
            notes: String? = null,
            chatId: String = ""
        ): ChatMessage {
            val statusMessage = when (newStatus.lowercase()) {
                STATUS_UPDATE_APPROVED -> "✅ Your quotation has been approved!"
                STATUS_UPDATE_REJECTED -> "❌ Your quotation has been rejected"
                STATUS_UPDATE_COMPLETED -> "🎉 Your quotation has been completed!"
                else -> "⏳ Your quotation is pending review"
            }

            val fullMessage = if (!notes.isNullOrEmpty()) {
                "$statusMessage\n\nAdmin Notes: $notes"
            } else {
                statusMessage
            }

            return ChatMessage(
                senderId = "admin",
                receiverId = quotation.userId,
                message = fullMessage,
                timestamp = System.currentTimeMillis(),
                quotationId = quotation.id,
                quotationStatus = newStatus,
                type = TYPE_STATUS_UPDATE,
                adminId = adminId,
                chatId = chatId
            )
        }

        /**
         * Creates a file attachment message
         */
        fun createFileMessage(
            senderId: String,
            receiverId: String,
            fileUri: String,
            fileName: String,
            fileSize: Long,
            mimeType: String? = null,
            chatId: String = ""
        ): ChatMessage {
            return ChatMessage(
                senderId = senderId,
                receiverId = receiverId,
                message = "📎 $fileName",
                timestamp = System.currentTimeMillis(),
                attachmentUri = fileUri,
                type = TYPE_FILE,
                fileName = fileName,
                fileSize = fileSize,
                mimeType = mimeType,
                chatId = chatId
            )
        }

        /**
         * Creates a file attachment message with sending status
         */
        fun createSendingFileMessage(
            senderId: String,
            receiverId: String,
            fileUri: String,
            fileName: String,
            fileSize: Long,
            mimeType: String? = null,
            chatId: String = ""
        ): ChatMessage {
            return ChatMessage(
                senderId = senderId,
                receiverId = receiverId,
                message = "📎 $fileName",
                timestamp = System.currentTimeMillis(),
                attachmentUri = fileUri,
                type = TYPE_FILE,
                status = STATUS_SENDING,
                fileName = fileName,
                fileSize = fileSize,
                mimeType = mimeType,
                chatId = chatId
            )
        }

        /**
         * Creates a system message
         */
        fun createSystemMessage(
            message: String,
            chatId: String = "",
            metadata: Map<String, String> = emptyMap()
        ): ChatMessage {
            return ChatMessage(
                senderId = "system",
                receiverId = "all",
                message = message,
                timestamp = System.currentTimeMillis(),
                type = TYPE_SYSTEM,
                chatId = chatId,
                metadata = metadata
            )
        }
    }

    /**
     * Gets formatted status update message
     */
    fun getStatusUpdateMessage(quotation: Quotation? = null): String {
        return when {
            isStatusUpdate && quotation != null -> {
                "Your quotation for ${quotation.companyName} has been ${quotationStatus ?: message}. Total: R${"%.2f".format(quotation.subtotal)}"
            }
            isStatusUpdate -> {
                "Quotation status updated: ${quotationStatus ?: message}"
            }
            else -> message
        }
    }

    /**
     * Marks message as sending
     */
    fun markAsSending(): ChatMessage {
        return this.copy(
            status = STATUS_SENDING
        )
    }

    /**
     * Marks message as sent
     */
    fun markAsSent(): ChatMessage {
        return this.copy(
            status = STATUS_SENT
        )
    }

    /**
     * Marks message as read
     */
    fun markAsRead(): ChatMessage {
        return this.copy(
            read = true,
            status = STATUS_READ
        )
    }

    /**
     * Marks message as delivered
     */
    fun markAsDelivered(): ChatMessage {
        return this.copy(
            status = STATUS_DELIVERED
        )
    }

    /**
     * Marks message as failed
     */
    fun markAsFailed(): ChatMessage {
        return this.copy(
            status = STATUS_FAILED
        )
    }

    /**
     * Edits the message content
     */
    fun edit(newMessage: String): ChatMessage {
        return this.copy(
            message = newMessage,
            edited = true,
            editedAt = System.currentTimeMillis()
        )
    }

    /**
     * Soft deletes the message
     */
    fun delete(): ChatMessage {
        return this.copy(
            deleted = true,
            message = "This message was deleted",
            attachmentUri = null,
            fileName = null,
            fileSize = 0L
        )
    }

    /**
     * Checks if message can be edited (within time limit)
     */
    fun canEdit(editTimeLimit: Long = 15 * 60 * 1000): Boolean {
        return !isSystemMessage && !deleted &&
                (System.currentTimeMillis() - timestamp) <= editTimeLimit
    }

    /**
     * Checks if message can be deleted
     */
    fun canDelete(deleteTimeLimit: Long = 5 * 60 * 1000): Boolean {
        return !isSystemMessage && !deleted &&
                (System.currentTimeMillis() - timestamp) <= deleteTimeLimit
    }

    /**
     * Validates message data
     */
    fun isValid(): Boolean {
        return senderId.isNotBlank() &&
                receiverId.isNotBlank() &&
                message.isNotBlank() &&
                timestamp > 0 &&
                !deleted
    }

    /**
     * Gets display name for sender
     */
    fun getSenderDisplayName(): String {
        return when {
            isSentByAdmin -> "Admin"
            senderId.contains("@") -> senderId.substringBefore("@")
            else -> "User"
        }
    }

    /**
     * Checks if this message is a continuation of previous message
     */
    fun isContinuation(previousMessage: ChatMessage?): Boolean {
        return previousMessage != null &&
                senderId == previousMessage.senderId &&
                (timestamp - previousMessage.timestamp) < 5 * 60 * 1000 && // 5 minutes
                !isSystemMessage &&
                !previousMessage.isSystemMessage
    }
}

// Extension functions for List<ChatMessage>
fun List<ChatMessage>.getUnreadCount(): Int {
    return this.count { !it.read && !it.isSentByAdmin }
}

fun List<ChatMessage>.getQuotationMessages(): List<ChatMessage> {
    return this.filter { it.isQuotationMessage }
}

fun List<ChatMessage>.getStatusUpdates(): List<ChatMessage> {
    return this.filter { it.isStatusUpdate }
}

fun List<ChatMessage>.getFileMessages(): List<ChatMessage> {
    return this.filter { it.isFileMessage || it.isImageMessage }
}

fun List<ChatMessage>.getLatestMessage(): ChatMessage? {
    return this.maxByOrNull { it.timestamp }
}

fun List<ChatMessage>.getLatestQuotationMessage(): ChatMessage? {
    return this.getQuotationMessages().maxByOrNull { it.timestamp }
}

fun List<ChatMessage>.markAllAsRead(): List<ChatMessage> {
    return this.map { it.markAsRead() }
}

fun List<ChatMessage>.groupByDate(): Map<String, List<ChatMessage>> {
    return this.groupBy { it.formattedDate }
}

fun List<ChatMessage>.getUnreadMessages(): List<ChatMessage> {
    return this.filter { !it.read && !it.isSentByAdmin }
}

fun List<ChatMessage>.getAdminMessages(): List<ChatMessage> {
    return this.filter { it.isSentByAdmin }
}

fun List<ChatMessage>.getUserMessages(): List<ChatMessage> {
    return this.filter { !it.isSentByAdmin && !it.isSystemMessage }
}

fun List<ChatMessage>.containsUnreadMessages(): Boolean {
    return this.any { !it.read && !it.isSentByAdmin }
}

fun List<ChatMessage>.getMessageById(messageId: String): ChatMessage? {
    return this.find { it.id == messageId }
}

fun List<ChatMessage>.getSendingMessages(): List<ChatMessage> {
    return this.filter { it.isSending }
}

/**
 * Creates a chat ID from two user IDs
 */
fun createChatId(user1: String, user2: String): String {
    return if (user1 < user2) "${user1}_$user2" else "${user2}_$user1"
}

/**
 * Encodes email for Firebase key compatibility
 */
fun encodeEmail(email: String): String {
    return email.replace(".", ",")
}

/**
 * Decodes email from Firebase key
 */
fun decodeEmail(encodedEmail: String): String {
    return encodedEmail.replace(",", ".")
}