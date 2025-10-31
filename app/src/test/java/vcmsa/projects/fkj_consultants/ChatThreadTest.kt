package vcmsa.projects.fkj_consultants

import org.junit.Before
import vcmsa.projects.fkj_consultants.models.ChatThread
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ChatThread model
 *
 * Tests cover:
 * - Thread creation and properties
 * - Unread count handling
 * - Timestamp validation
 * - Thread identification
 */
class ChatThreadTest {

    private lateinit var basicThread: ChatThread
    private lateinit var threadWithUnread: ChatThread

    @Before
    fun setup() {
        basicThread = ChatThread(
            chatId = "user,example,com",
            userEmail = "user@example.com",
            adminEmail = "admin@example.com",
            lastMessage = "Hello from user",
            lastTimestamp = 1699900000000L,
            userId = "user123",
            unreadCount = 0
        )

        threadWithUnread = ChatThread(
            chatId = "customer,test,com",
            userEmail = "customer@test.com",
            adminEmail = "admin@example.com",
            lastMessage = "Need help with quotation",
            lastTimestamp = 1699900100000L,
            userId = "user456",
            unreadCount = 5
        )
    }

    @Test
    fun `test thread basic properties`() {
        assertEquals("user,example,com", basicThread.chatId)
        assertEquals("user@example.com", basicThread.userEmail)
        assertEquals("admin@example.com", basicThread.adminEmail)
        assertEquals("Hello from user", basicThread.lastMessage)
        assertEquals(1699900000000L, basicThread.lastTimestamp)
        assertEquals("user123", basicThread.userId)
        assertEquals(0, basicThread.unreadCount)
    }

    @Test
    fun `test thread with unread messages`() {
        assertEquals(5, threadWithUnread.unreadCount)
        assertTrue(threadWithUnread.unreadCount > 0)
    }

    @Test
    fun `test thread with no unread messages`() {
        assertEquals(0, basicThread.unreadCount)
        assertFalse(basicThread.unreadCount > 0)
    }

    @Test
    fun `test thread timestamp comparison`() {
        assertTrue(threadWithUnread.lastTimestamp > basicThread.lastTimestamp)
    }

    @Test
    fun `test chatId encoding format`() {
        assertTrue(basicThread.chatId.contains(","))
        assertFalse(basicThread.chatId.contains("."))
    }

    @Test
    fun `test thread with empty last message`() {
        val thread = ChatThread(
            chatId = "test,com",
            userEmail = "test@example.com",
            adminEmail = "admin@example.com",
            lastMessage = "",
            lastTimestamp = System.currentTimeMillis(),
            userId = "user789",
            unreadCount = 0
        )

        assertEquals("", thread.lastMessage)
    }

    @Test
    fun `test thread sorting by timestamp`() {
        val thread1 = basicThread
        val thread2 = threadWithUnread

        val sorted = listOf(thread1, thread2).sortedByDescending { it.lastTimestamp }
        assertEquals(thread2, sorted[0])
        assertEquals(thread1, sorted[1])
    }

    @Test
    fun `test thread with multiple unread messages`() {
        val thread = ChatThread(
            chatId = "test,com",
            userEmail = "test@example.com",
            adminEmail = "admin@example.com",
            lastMessage = "Multiple unread",
            lastTimestamp = System.currentTimeMillis(),
            userId = "user999",
            unreadCount = 25
        )

        assertEquals(25, thread.unreadCount)
    }
}
// (Gideon, 2023).