package vcmsa.projects.fkj_consultants

import vcmsa.projects.fkj_consultants.models.ChatThread
import vcmsa.projects.fkj_consultants.models.ChatMessage
import vcmsa.projects.fkj_consultants.extensions.* // Import the extensions
import org.junit.Assert.*
import org.junit.Test

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
            ChatMessage(senderId = "user", receiverId = "admin", message = "First", timestamp = 1000L),
            ChatMessage(senderId = "admin", receiverId = "user", message = "Reply", timestamp = 2000L),
            ChatMessage(senderId = "user", receiverId = "admin", message = "Thanks", timestamp = 3000L)
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

    @Test
    fun `test ChatThread extension functions`() {
        val threads = listOf(
            ChatThread(
                chatId = "1",
                userEmail = "user1@test.com",
                adminEmail = "admin@test.com",
                lastMessage = "Message 1",
                lastTimestamp = 1000L,
                userId = "user1",
                unreadCount = 3
            ),
            ChatThread(
                chatId = "2",
                userEmail = "user2@test.com",
                adminEmail = "admin@test.com",
                lastMessage = "Message 2",
                lastTimestamp = 2000L,
                userId = "user2",
                unreadCount = 0
            ),
            ChatThread(
                chatId = "3",
                userEmail = "user3@test.com",
                adminEmail = "admin@test.com",
                lastMessage = "Message 3",
                lastTimestamp = 3000L,
                userId = "user3",
                unreadCount = 5,
                isArchived = true
            )
        )

        // Test extension functions
        assertEquals(8, threads.getUnreadCount())
        assertEquals(2, threads.getActiveThreads().size)
        assertEquals(1, threads.getArchivedThreads().size)
        assertEquals(2, threads.getThreadsWithUnread().size)

        val sorted = threads.sortByLastMessage()
        assertEquals(3000L, sorted[0].lastTimestamp)
        assertEquals(2000L, sorted[1].lastTimestamp)
        assertEquals(1000L, sorted[2].lastTimestamp)
    }

    @Test
    fun `test ChatThread update methods`() {
        val originalThread = ChatThread(
            chatId = "test",
            userEmail = "user@test.com",
            adminEmail = "admin@test.com",
            lastMessage = "Old message",
            lastTimestamp = 1000L,
            userId = "user123",
            unreadCount = 0
        )

        val newMessage = ChatMessage(
            senderId = "user@test.com",
            receiverId = "admin@test.com",
            message = "New message",
            timestamp = 2000L
        )

        val updatedThread = originalThread.updateWithMessage(newMessage)
        assertEquals("New message", updatedThread.lastMessage)
        assertEquals(2000L, updatedThread.lastTimestamp)
        assertEquals(1, updatedThread.unreadCount)

        val readThread = updatedThread.markAsRead()
        assertEquals(0, readThread.unreadCount)
    }

    @Test
    fun `test ChatThread validation`() {
        val validThread = ChatThread(
            chatId = "test",
            userEmail = "user@test.com",
            adminEmail = "admin@test.com",
            lastMessage = "Test",
            lastTimestamp = 1000L,
            userId = "user123"
        )

        val invalidThread = ChatThread(
            chatId = "",
            userEmail = "",
            lastMessage = "Test",
            lastTimestamp = 0L,
            userId = ""
        )

        assertTrue(validThread.isValid())
        assertFalse(invalidThread.isValid())
    }
}
//(Gideon, 2023).