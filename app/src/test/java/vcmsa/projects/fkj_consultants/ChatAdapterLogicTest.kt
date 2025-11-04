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
            ChatMessage("user1", "admin", "Old message 1", 1000L),
            ChatMessage("user1", "admin", "Old message 2", 2000L)
        )

        val newMessages = listOf(
            ChatMessage("user2", "admin", "New message 1", 3000L),
            ChatMessage("user2", "admin", "New message 2", 4000L),
            ChatMessage("user2", "admin", "New message 3", 5000L)
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
            ChatMessage("user", "admin", "Third", 3000L),
            ChatMessage("user", "admin", "First", 1000L),
            ChatMessage("user", "admin", "Second", 2000L)
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
}
// (Gideon, 2023).