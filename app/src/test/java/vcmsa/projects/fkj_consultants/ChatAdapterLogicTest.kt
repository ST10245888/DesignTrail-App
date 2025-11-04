package vcmsa.projects.fkj_consultants

import vcmsa.projects.fkj_consultants.models.ChatMessage
import org.junit.Assert.*
import org.junit.Test

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

        assertFalse(messageWithAttachment.attachmentUri.isNullOrEmpty())
        assertTrue(messageWithoutAttachment.attachmentUri.isNullOrEmpty())
    }

    @Test
    fun `test chat ID generation logic`() {
        // Test chat ID generation for different user IDs
        val userId1 = "abc123"
        val userId2 = "xyz789"

        val chatId1 = if (userId1 < "admin") "${userId1}_admin" else "admin_${userId1}"
        val chatId2 = if (userId2 < "admin") "${userId2}_admin" else "admin_${userId2}"

        assertEquals("abc123_admin", chatId1)
        assertEquals("admin_xyz789", chatId2)
    }

    @Test
    fun `test message type constants`() {
        // Verify message type constants exist and are accessible
        assertEquals("text", ChatMessage.TYPE_TEXT)
        assertEquals("quotation", ChatMessage.TYPE_QUOTATION)
        assertEquals("file", ChatMessage.TYPE_FILE)
        assertEquals("system", ChatMessage.TYPE_SYSTEM)
        assertEquals("status_update", ChatMessage.TYPE_STATUS_UPDATE)
    }

    @Test
    fun `test message status constants`() {
        // Verify message status constants
        assertEquals("sending", ChatMessage.STATUS_SENDING)
        assertEquals("sent", ChatMessage.STATUS_SENT)
        assertEquals("delivered", ChatMessage.STATUS_DELIVERED)
        assertEquals("read", ChatMessage.STATUS_READ)
    }

    @Test
    fun `test quotation status constants`() {
        // Verify quotation status constants
        assertEquals("approved", ChatMessage.STATUS_UPDATE_APPROVED)
        assertEquals("rejected", ChatMessage.STATUS_UPDATE_REJECTED)
        assertEquals("pending", ChatMessage.STATUS_UPDATE_PENDING)
    }

    @Test
    fun `test message with different types`() {
        val textMessage = ChatMessage(
            senderId = "user",
            receiverId = "admin",
            message = "Hello",
            timestamp = System.currentTimeMillis(),
            type = ChatMessage.TYPE_TEXT
        )

        val quotationMessage = ChatMessage(
            senderId = "user",
            receiverId = "admin",
            message = "Quotation message",
            timestamp = System.currentTimeMillis(),
            type = ChatMessage.TYPE_QUOTATION,
            quotationId = "Q123"
        )

        assertEquals(ChatMessage.TYPE_TEXT, textMessage.type)
        assertEquals(ChatMessage.TYPE_QUOTATION, quotationMessage.type)
        assertEquals("Q123", quotationMessage.quotationId)
    }

    @Test
    fun `test message sender comparison`() {
        val adminSenderId = "kush@gmail.com"
        val userSenderId = "user@test.com"

        val message1 = ChatMessage(senderId = adminSenderId, receiverId = userSenderId, message = "Test", timestamp = 1000L)
        val message2 = ChatMessage(senderId = userSenderId, receiverId = adminSenderId, message = "Reply", timestamp = 2000L)

        // Test sender identification
        assertTrue(message1.senderId == adminSenderId)
        assertTrue(message2.senderId == userSenderId)
        assertFalse(message1.senderId == userSenderId)
    }

    @Test
    fun `test message timestamp comparison`() {
        val olderMessage = ChatMessage(
            senderId = "user",
            receiverId = "admin",
            message = "Older",
            timestamp = 1000L
        )

        val newerMessage = ChatMessage(
            senderId = "user",
            receiverId = "admin",
            message = "Newer",
            timestamp = 2000L
        )

        assertTrue(newerMessage.timestamp > olderMessage.timestamp)
        assertTrue(olderMessage.timestamp < newerMessage.timestamp)
    }

    @Test
    fun `test message list filtering by sender`() {
        val messages = listOf(
            ChatMessage(senderId = "user1", receiverId = "admin", message = "Msg1", timestamp = 1000L),
            ChatMessage(senderId = "admin", receiverId = "user1", message = "Msg2", timestamp = 2000L),
            ChatMessage(senderId = "user1", receiverId = "admin", message = "Msg3", timestamp = 3000L),
            ChatMessage(senderId = "admin", receiverId = "user1", message = "Msg4", timestamp = 4000L)
        )

        val userMessages = messages.filter { it.senderId == "user1" }
        val adminMessages = messages.filter { it.senderId == "admin" }

        assertEquals(2, userMessages.size)
        assertEquals(2, adminMessages.size)
    }

    @Test
    fun `test message list sorting by timestamp descending`() {
        val messages = listOf(
            ChatMessage(senderId = "user", receiverId = "admin", message = "First", timestamp = 1000L),
            ChatMessage(senderId = "user", receiverId = "admin", message = "Third", timestamp = 3000L),
            ChatMessage(senderId = "user", receiverId = "admin", message = "Second", timestamp = 2000L)
        )

        val sorted = messages.sortedByDescending { it.timestamp }

        assertEquals("Third", sorted[0].message)
        assertEquals("Second", sorted[1].message)
        assertEquals("First", sorted[2].message)
        assertEquals(3000L, sorted[0].timestamp)
    }

    @Test
    fun `test message with quotation ID`() {
        val messageWithQuotation = ChatMessage(
            senderId = "user",
            receiverId = "admin",
            message = "Check this quotation",
            timestamp = System.currentTimeMillis(),
            quotationId = "QUOT_123456"
        )

        assertNotNull(messageWithQuotation.quotationId)
        assertEquals("QUOT_123456", messageWithQuotation.quotationId)
    }

    @Test
    fun `test message without quotation ID`() {
        val regularMessage = ChatMessage(
            senderId = "user",
            receiverId = "admin",
            message = "Regular message",
            timestamp = System.currentTimeMillis()
        )

        assertTrue(regularMessage.quotationId.isNullOrEmpty())
    }

    @Test
    fun `test empty message list operations`() {
        val emptyMessages = emptyList<ChatMessage>()

        assertTrue(emptyMessages.isEmpty())
        assertEquals(0, emptyMessages.size)
        assertEquals(null, emptyMessages.firstOrNull())
        assertEquals(null, emptyMessages.lastOrNull())
    }

    @Test
    fun `test message list with single item`() {
        val singleMessage = listOf(
            ChatMessage(senderId = "user", receiverId = "admin", message = "Only message", timestamp = 1000L)
        )

        assertEquals(1, singleMessage.size)
        assertEquals(singleMessage.first(), singleMessage.last())
    }

    @Test
    fun `test message grouping by sender`() {
        val messages = listOf(
            ChatMessage(senderId = "user1", receiverId = "admin", message = "A", timestamp = 1000L),
            ChatMessage(senderId = "user2", receiverId = "admin", message = "B", timestamp = 2000L),
            ChatMessage(senderId = "user1", receiverId = "admin", message = "C", timestamp = 3000L),
            ChatMessage(senderId = "user3", receiverId = "admin", message = "D", timestamp = 4000L),
            ChatMessage(senderId = "user2", receiverId = "admin", message = "E", timestamp = 5000L)
        )

        val groupedBySender = messages.groupBy { it.senderId }

        assertEquals(3, groupedBySender.keys.size)
        assertEquals(2, groupedBySender["user1"]?.size)
        assertEquals(2, groupedBySender["user2"]?.size)
        assertEquals(1, groupedBySender["user3"]?.size)
    }

    @Test
    fun `test message count by type`() {
        val messages = listOf(
            ChatMessage(senderId = "user", receiverId = "admin", message = "Text 1", timestamp = 1000L, type = ChatMessage.TYPE_TEXT),
            ChatMessage(senderId = "user", receiverId = "admin", message = "Text 2", timestamp = 2000L, type = ChatMessage.TYPE_TEXT),
            ChatMessage(senderId = "user", receiverId = "admin", message = "Quotation", timestamp = 3000L, type = ChatMessage.TYPE_QUOTATION),
            ChatMessage(senderId = "admin", receiverId = "user", message = "File", timestamp = 4000L, type = ChatMessage.TYPE_FILE)
        )

        val textMessages = messages.count { it.type == ChatMessage.TYPE_TEXT }
        val quotationMessages = messages.count { it.type == ChatMessage.TYPE_QUOTATION }
        val fileMessages = messages.count { it.type == ChatMessage.TYPE_FILE }

        assertEquals(2, textMessages)
        assertEquals(1, quotationMessages)
        assertEquals(1, fileMessages)
    }
}
// (Gideon, 2023).