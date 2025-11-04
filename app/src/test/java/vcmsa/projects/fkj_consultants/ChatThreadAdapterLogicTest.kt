package vcmsa.projects.fkj_consultants

import org.junit.Assert.*
import org.junit.Test
import vcmsa.projects.fkj_consultants.models.ChatMessage

/**
 * Unit tests for ChatAdapter functionality
 *
 * Tests the adapter's message management and view type logic
 */
class ChatAdapterLogicTest {

    @Test
    fun `test message list initialization`() {
        val messages = mutableListOf<ChatMessage>()
        assertTrue(messages.isEmpty())
    }

    @Test
    fun `test adding message to list`() {
        val messages = mutableListOf<ChatMessage>()
        val message = ChatMessage(
            senderId = "user@test.com",
            receiverId = "admin@test.com",
            message = "Test",
            timestamp = System.currentTimeMillis()
        )

        messages.add(message)
        assertEquals(1, messages.size)
        assertEquals(message, messages[0])
    }

    @Test
    fun `test setting messages replaces list`() {
        val messages = mutableListOf(
            ChatMessage(senderId = "user1", receiverId = "admin", message = "Old message 1", timestamp = 1000L),
            ChatMessage(senderId = "user1", receiverId = "admin", message = "Old message 2", timestamp = 2000L)
        )

        val newMessages = listOf(
            ChatMessage(senderId = "user2", receiverId = "admin", message = "New message 1", timestamp = 3000L),
            ChatMessage(senderId = "user2", receiverId = "admin", message = "New message 2", timestamp = 4000L),
            ChatMessage(senderId = "user2", receiverId = "admin", message = "New message 3", timestamp = 5000L)
        )

        messages.clear()
        messages.addAll(newMessages)

        assertEquals(3, messages.size)
        assertEquals("New message 1", messages[0].message)
    }

    @Test
    fun `test view type determination for sent message`() {
        val currentUserId = "user@test.com"
        val message = ChatMessage(
            senderId = currentUserId,
            receiverId = "admin@test.com",
            message = "My message",
            timestamp = System.currentTimeMillis()
        )

        val isSent = message.senderId == currentUserId
        assertTrue(isSent)
    }

    @Test
    fun `test view type determination for received message`() {
        val currentUserId = "user@test.com"
        val message = ChatMessage(
            senderId = "admin@test.com",
            receiverId = currentUserId,
            message = "Admin reply",
            timestamp = System.currentTimeMillis()
        )

        val isSent = message.senderId == currentUserId
        assertFalse(isSent)
    }

    @Test
    fun `test messages sorted by timestamp`() {
        val messages = listOf(
            ChatMessage(senderId = "user", receiverId = "admin", message = "Third", timestamp = 3000L),
            ChatMessage(senderId = "user", receiverId = "admin", message = "First", timestamp = 1000L),
            ChatMessage(senderId = "user", receiverId = "admin", message = "Second", timestamp = 2000L)
        )

        val sorted = messages.sortedBy { it.timestamp }
        assertEquals("First", sorted[0].message)
        assertEquals("Second", sorted[1].message)
        assertEquals("Third", sorted[2].message)
    }

    @Test
    fun `test attachment message identification`() {
        val messageWithAttachment = ChatMessage(
            senderId = "user@test.com",
            receiverId = "admin@test.com",
            message = "File attached",
            timestamp = System.currentTimeMillis(),
            attachmentUri = "/storage/file.pdf"
        )

        val messageWithoutAttachment = ChatMessage(
            senderId = "user@test.com",
            receiverId = "admin@test.com",
            message = "No attachment",
            timestamp = System.currentTimeMillis()
        )

        assertTrue(messageWithAttachment.hasAttachment)
        assertFalse(messageWithoutAttachment.hasAttachment)
    }

    @Test
    fun `test admin message detection`() {
        val adminMessage = ChatMessage(
            senderId = "kush@gmail.com",
            receiverId = "user@test.com",
            message = "Admin message",
            timestamp = System.currentTimeMillis()
        )

        val userMessage = ChatMessage(
            senderId = "user@test.com",
            receiverId = "admin@gmail.com",
            message = "User message",
            timestamp = System.currentTimeMillis()
        )

        assertTrue(adminMessage.isSentByAdmin)
        assertFalse(userMessage.isSentByAdmin)
    }

    @Test
    fun `test message status updates`() {
        val message = ChatMessage(
            status = ChatMessage.STATUS_SENT
        )

        val delivered = message.markAsDelivered()
        val read = delivered.markAsRead()

        assertEquals(ChatMessage.STATUS_DELIVERED, delivered.status)
        assertEquals(ChatMessage.STATUS_READ, read.status)
        assertTrue(read.read)
    }

    @Test
    fun `test message validation`() {
        val validMessage = ChatMessage(
            senderId = "user@test.com",
            receiverId = "admin@test.com",
            message = "Valid message",
            timestamp = System.currentTimeMillis()
        )

        val invalidMessage = ChatMessage(
            senderId = "",
            receiverId = "admin@test.com",
            message = "Invalid message",
            timestamp = System.currentTimeMillis()
        )

        assertTrue(validMessage.isValid())
        assertFalse(invalidMessage.isValid())
    }

    @Test
    fun `test message type detection`() {
        val textMessage = ChatMessage(type = ChatMessage.TYPE_TEXT)
        val quotationMessage = ChatMessage(type = ChatMessage.TYPE_QUOTATION)
        val fileMessage = ChatMessage(type = ChatMessage.TYPE_FILE)
        val systemMessage = ChatMessage(type = ChatMessage.TYPE_SYSTEM)

        assertTrue(textMessage.isTextMessage)
        assertTrue(quotationMessage.isQuotationMessage)
        assertTrue(fileMessage.isFileMessage)
        assertTrue(systemMessage.isSystemMessage)
    }

    @Test
    fun `test message timestamp formatting`() {
        val message = ChatMessage(timestamp = 1704067200000L) // 2024-01-01 00:00:00 UTC

        assertTrue(message.formattedTimestamp.isNotEmpty())
        assertTrue(message.formattedDate.isNotEmpty())
        assertTrue(message.formattedTimestamp.contains("Jan"))
    }

    @Test
    fun `test message list operations`() {
        val messages = listOf(
            ChatMessage(senderId = "user", read = false),
            ChatMessage(senderId = "admin", read = false),
            ChatMessage(senderId = "user", read = true),
            ChatMessage(senderId = "admin", read = true, type = ChatMessage.TYPE_QUOTATION)
        )

        // Test using the built-in computed properties and manual filtering
        val unreadCount = messages.count { !it.read && !it.isSentByAdmin }
        val quotationMessages = messages.filter { it.isQuotationMessage }
        val userMessages = messages.filter { !it.isSentByAdmin && !it.isSystemMessage }
        val adminMessages = messages.filter { it.isSentByAdmin }
        val hasUnread = messages.any { !it.read && !it.isSentByAdmin }

        assertEquals(1, unreadCount)
        assertEquals(1, quotationMessages.size)
        assertEquals(2, userMessages.size)
        assertEquals(2, adminMessages.size)
        assertTrue(hasUnread)
    }

    @Test
    fun `test message continuation logic`() {
        val firstMessage = ChatMessage(
            senderId = "user@test.com",
            timestamp = System.currentTimeMillis() - 120000 // 2 minutes ago
        )

        val continuationMessage = ChatMessage(
            senderId = "user@test.com",
            timestamp = System.currentTimeMillis() - 60000 // 1 minute ago
        )

        val nonContinuationMessage = ChatMessage(
            senderId = "admin@test.com",
            timestamp = System.currentTimeMillis() - 30000 // 30 seconds ago
        )

        assertTrue(continuationMessage.isContinuation(firstMessage))
        assertFalse(nonContinuationMessage.isContinuation(continuationMessage))
    }

    @Test
    fun `test message with sending status`() {
        val sendingMessage = ChatMessage(
            senderId = "user@test.com",
            receiverId = "admin@test.com",
            message = "Sending...",
            status = ChatMessage.STATUS_SENDING
        )

        assertTrue(sendingMessage.isSending)
        assertEquals(ChatMessage.STATUS_SENDING, sendingMessage.status)
    }

    @Test
    fun `test message copy operations`() {
        val original = ChatMessage(
            senderId = "user@test.com",
            receiverId = "admin@test.com",
            message = "Original",
            status = ChatMessage.STATUS_SENDING
        )

        val sent = original.markAsSent()
        val delivered = sent.markAsDelivered()
        val read = delivered.markAsRead()

        assertEquals(ChatMessage.STATUS_SENT, sent.status)
        assertEquals(ChatMessage.STATUS_DELIVERED, delivered.status)
        assertEquals(ChatMessage.STATUS_READ, read.status)
        assertTrue(read.read)
    }

    @Test
    fun `test message edit and delete operations`() {
        val originalMessage = ChatMessage(
            senderId = "user@test.com",
            receiverId = "admin@test.com",
            message = "Original message",
            timestamp = System.currentTimeMillis()
        )

        val editedMessage = originalMessage.edit("Edited message")
        val deletedMessage = originalMessage.delete()

        assertTrue(editedMessage.edited)
        assertNotNull(editedMessage.editedAt)
        assertEquals("Edited message", editedMessage.message)

        assertTrue(deletedMessage.deleted)
        assertEquals("This message was deleted", deletedMessage.message)
        assertNull(deletedMessage.attachmentUri)
    }

    @Test
    fun `test message can edit and delete within time limits`() {
        val recentMessage = ChatMessage(
            senderId = "user@test.com",
            receiverId = "admin@test.com",
            message = "Recent message",
            timestamp = System.currentTimeMillis() - 60000 // 1 minute ago
        )

        val oldMessage = ChatMessage(
            senderId = "user@test.com",
            receiverId = "admin@test.com",
            message = "Old message",
            timestamp = System.currentTimeMillis() - 10 * 60 * 1000 // 10 minutes ago
        )

        assertTrue(recentMessage.canEdit())
        assertTrue(recentMessage.canDelete())
        assertFalse(oldMessage.canEdit())
        assertFalse(oldMessage.canDelete())
    }

    @Test
    fun `test sender display name`() {
        val adminMessage = ChatMessage(senderId = "kush@gmail.com")
        val userMessage = ChatMessage(senderId = "user@test.com")
        val genericMessage = ChatMessage(senderId = "user123")

        assertEquals("Admin", adminMessage.getSenderDisplayName())
        assertEquals("user", userMessage.getSenderDisplayName())
        assertEquals("User", genericMessage.getSenderDisplayName())
    }

    @Test
    fun `test file size formatting`() {
        val smallFile = ChatMessage(fileSize = 500L)
        val mediumFile = ChatMessage(fileSize = 1024 * 512L) // 512 KB
        val largeFile = ChatMessage(fileSize = 1024 * 1024 * 2L) // 2 MB

        assertEquals("500 B", smallFile.fileSizeFormatted)
        assertTrue(mediumFile.fileSizeFormatted.contains("KB"))
        assertTrue(largeFile.fileSizeFormatted.contains("MB"))
    }

    @Test
    fun `test quotation status update message`() {
        val quotationMessage = ChatMessage(
            type = ChatMessage.TYPE_STATUS_UPDATE,
            quotationStatus = ChatMessage.STATUS_UPDATE_APPROVED,
            message = "Your quotation has been approved!"
        )

        assertTrue(quotationMessage.isStatusUpdate)
        assertTrue(quotationMessage.isQuotationStatusUpdate)
        assertEquals(ChatMessage.STATUS_UPDATE_APPROVED, quotationMessage.quotationStatus)
    }

    @Test
    fun `test system message properties`() {
        val systemMessage = ChatMessage(
            type = ChatMessage.TYPE_SYSTEM,
            senderId = "system",
            message = "User joined the chat"
        )

        assertTrue(systemMessage.isSystemMessage)
        assertEquals("system", systemMessage.senderId)
        assertFalse(systemMessage.canEdit())
        assertFalse(systemMessage.canDelete())
    }
}

// If the extension functions are not available in the models package,
// you can add them here as a temporary solution:
/*
// Extension functions for List<ChatMessage>
fun List<ChatMessage>.getUnreadCount(): Int {
    return this.count { !it.read && !it.isSentByAdmin }
}

fun List<ChatMessage>.getQuotationMessages(): List<ChatMessage> {
    return this.filter { it.isQuotationMessage }
}

fun List<ChatMessage>.getUserMessages(): List<ChatMessage> {
    return this.filter { !it.isSentByAdmin && !it.isSystemMessage }
}

fun List<ChatMessage>.getAdminMessages(): List<ChatMessage> {
    return this.filter { it.isSentByAdmin }
}

fun List<ChatMessage>.containsUnreadMessages(): Boolean {
    return this.any { !it.read && !it.isSentByAdmin }
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

fun List<ChatMessage>.getMessageById(messageId: String): ChatMessage? {
    return this.find { it.id == messageId }
}
*/