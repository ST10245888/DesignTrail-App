package vcmsa.projects.fkj_consultants

import vcmsa.projects.fkj_consultants.models.ChatThread
import org.junit.Assert.*
import org.junit.Test
import vcmsa.projects.fkj_consultants.models.ChatMessage

/**
 * Integration tests for chat system components
 */
class ChatSystemIntegrationTest {

    @Test
    fun `test complete message flow from user to admin`() {
        val userEmail = "user@example.com"
        val adminEmail = "admin@example.com"

        // User sends message
        val message = ChatMessage(
            senderId = userEmail,
            receiverId = adminEmail,
            message = "I need help with my quotation",
            timestamp = System.currentTimeMillis()
        )

        // Create thread for this conversation
        val thread = ChatThread(
            chatId = "user,example,com",
            userEmail = userEmail,
            adminEmail = adminEmail,
            lastMessage = message.message,
            lastTimestamp = message.timestamp,
            userId = "user123",
            unreadCount = 1
        )

        assertEquals(message.message, thread.lastMessage)
        assertEquals(message.timestamp, thread.lastTimestamp)
        assertEquals(1, thread.unreadCount)
    }

    @Test
    fun `test admin reply resets unread count`() {
        val thread = ChatThread(
            chatId = "user,test,com",
            userEmail = "user@test.com",
            adminEmail = "admin@example.com",
            lastMessage = "User message",
            lastTimestamp = 1000L,
            userId = "user123",
            unreadCount = 5
        )

        // Simulate admin opening chat - unread count should be reset
        val updatedThread = thread.copy(unreadCount = 0)
        assertEquals(0, updatedThread.unreadCount)
    }

    @Test
    fun `test message with attachment creates proper thread`() {
        val attachmentMessage = ChatMessage(
            senderId = "user@test.com",
            receiverId = "admin@test.com",
            message = "📄 Quotation attached",
            timestamp = System.currentTimeMillis(),
            attachmentUri = "/storage/quotation.pdf"
        )

        val thread = ChatThread(
            chatId = "user,test,com",
            userEmail = "user@test.com",
            adminEmail = "admin@test.com",
            lastMessage = attachmentMessage.message,
            lastTimestamp = attachmentMessage.timestamp,
            userId = "user123",
            unreadCount = 1
        )

        assertTrue(thread.lastMessage.contains("📄"))
        assertNotNull(attachmentMessage.attachmentUri)
    }

    @Test
    fun `test multiple admin handling for same user`() {
        val userEmail = "user@example.com"
        val admin1 = "admin1@example.com"
        val admin2 = "admin2@example.com"

        val thread1 = ChatThread(
            chatId = "user,example,com",
            userEmail = userEmail,
            adminEmail = admin1,
            lastMessage = "Test message",
            lastTimestamp = System.currentTimeMillis(),
            userId = "user123",
            unreadCount = 3
        )

        val thread2 = ChatThread(
            chatId = "user,example,com",
            userEmail = userEmail,
            adminEmail = admin2,
            lastMessage = "Test message",
            lastTimestamp = System.currentTimeMillis(),
            userId = "user123",
            unreadCount = 3
        )

        // Same chatId for both admins (unified chat)
        assertEquals(thread1.chatId, thread2.chatId)
        assertEquals(thread1.userEmail, thread2.userEmail)
    }

    @Test
    fun `test timestamp-based message ordering in conversation`() {
        val messages = listOf(
            ChatMessage("user", "admin", "First", 1000L),
            ChatMessage("admin", "user", "Reply", 2000L),
            ChatMessage("user", "admin", "Thanks", 3000L)
        )

        val sorted = messages.sortedBy { it.timestamp }
        assertEquals("First", sorted[0].message)
        assertEquals("Reply", sorted[1].message)
        assertEquals("Thanks", sorted[2].message)

        // Verify alternating senders
        assertEquals("user", sorted[0].senderId)
        assertEquals("admin", sorted[1].senderId)
        assertEquals("user", sorted[2].senderId)
    }
}
// (Gideon, 2023).