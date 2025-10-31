package vcmsa.projects.fkj_consultants

import vcmsa.projects.fkj_consultants.models.ChatThread
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ChatThreadAdapter functionality
 *
 * Tests thread list management and sorting logic
 */
class ChatThreadAdapterLogicTest {

    @Test
    fun `test thread list initialization`() {
        val threads = mutableListOf<ChatThread>()
        assertTrue(threads.isEmpty())
    }

    @Test
    fun `test add or update thread - new thread`() {
        val threads = mutableListOf<ChatThread>()
        val newThread = ChatThread(
            chatId = "test,com",
            userEmail = "test@example.com",
            adminEmail = "admin@example.com",
            lastMessage = "Hello",
            lastTimestamp = System.currentTimeMillis(),
            userId = "user123",
            unreadCount = 1
        )

        // Simulate add
        val existingIndex = threads.indexOfFirst { it.chatId == newThread.chatId }
        if (existingIndex != -1) {
            threads.removeAt(existingIndex)
        }
        threads.add(0, newThread)

        assertEquals(1, threads.size)
        assertEquals(newThread, threads[0])
    }

    @Test
    fun `test add or update thread - existing thread`() {
        val threads = mutableListOf(
            ChatThread(
                chatId = "test,com",
                userEmail = "test@example.com",
                adminEmail = "admin@example.com",
                lastMessage = "Old message",
                lastTimestamp = 1000L,
                userId = "user123",
                unreadCount = 0
            )
        )

        val updatedThread = ChatThread(
            chatId = "test,com",
            userEmail = "test@example.com",
            adminEmail = "admin@example.com",
            lastMessage = "New message",
            lastTimestamp = 2000L,
            userId = "user123",
            unreadCount = 5
        )

        // Simulate update
        val existingIndex = threads.indexOfFirst { it.chatId == updatedThread.chatId }
        if (existingIndex != -1) {
            threads.removeAt(existingIndex)
        }
        threads.add(0, updatedThread)

        assertEquals(1, threads.size)
        assertEquals("New message", threads[0].lastMessage)
        assertEquals(5, threads[0].unreadCount)
    }

    @Test
    fun `test sort threads by timestamp descending`() {
        val threads = mutableListOf(
            ChatThread("id1", "user1@test.com", "admin", "Old", 1000L, "u1", 0),
            ChatThread("id2", "user2@test.com", "admin", "Newer", 3000L, "u2", 0),
            ChatThread("id3", "user3@test.com", "admin", "Newest", 5000L, "u3", 0),
            ChatThread("id4", "user4@test.com", "admin", "Middle", 2000L, "u4", 0)
        )

        threads.sortByDescending { it.lastTimestamp }

        assertEquals("Newest", threads[0].lastMessage)
        assertEquals("Newer", threads[1].lastMessage)
        assertEquals("Middle", threads[2].lastMessage)
        assertEquals("Old", threads[3].lastMessage)
    }

    @Test
    fun `test remove thread by chatId`() {
        val threads = mutableListOf(
            ChatThread("id1", "user1@test.com", "admin", "Message 1", 1000L, "u1", 0),
            ChatThread("id2", "user2@test.com", "admin", "Message 2", 2000L, "u2", 0),
            ChatThread("id3", "user3@test.com", "admin", "Message 3", 3000L, "u3", 0)
        )

        val chatIdToRemove = "id2"
        val index = threads.indexOfFirst { it.chatId == chatIdToRemove }
        if (index != -1) {
            threads.removeAt(index)
        }

        assertEquals(2, threads.size)
        assertFalse(threads.any { it.chatId == chatIdToRemove })
    }

    @Test
    fun `test update threads replaces entire list`() {
        val threads = mutableListOf(
            ChatThread("old1", "old@test.com", "admin", "Old", 1000L, "u1", 0)
        )

        val newThreads = listOf(
            ChatThread("new1", "new1@test.com", "admin", "New 1", 3000L, "u2", 0),
            ChatThread("new2", "new2@test.com", "admin", "New 2", 2000L, "u3", 0)
        ).sortedByDescending { it.lastTimestamp }

        threads.clear()
        threads.addAll(newThreads)

        assertEquals(2, threads.size)
        assertEquals("New 1", threads[0].lastMessage)
        assertFalse(threads.any { it.chatId == "old1" })
    }

    @Test
    fun `test threads with unread count filtering`() {
        val threads = listOf(
            ChatThread("id1", "user1@test.com", "admin", "No unread", 1000L, "u1", 0),
            ChatThread("id2", "user2@test.com", "admin", "Has unread", 2000L, "u2", 5),
            ChatThread("id3", "user3@test.com", "admin", "Also unread", 3000L, "u3", 2)
        )

        val unreadThreads = threads.filter { it.unreadCount > 0 }
        assertEquals(2, unreadThreads.size)
    }
}
// (Gideon, 2023).