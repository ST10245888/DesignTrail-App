package vcmsa.projects.fkj_consultants

import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import com.google.firebase.database.DatabaseReference
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import vcmsa.projects.fkj_consultants.adapters.ChatMessageAdapter
import vcmsa.projects.fkj_consultants.models.ChatMessage

class UserMessagingTest {

    private lateinit var mockMessagesRef: DatabaseReference
    private lateinit var adapter: ChatMessageAdapter

    private val currentUserEmail = "user@example.com"
    private val adminEmail = "admin@example.com"

    @Before
    fun setup() {
        mockMessagesRef = mock()
        // Pass notify=false to avoid RecyclerView issues in JVM tests
        adapter = ChatMessageAdapter(mutableListOf(), currentUserEmail)
    }

    @Test
    fun `user can send message and adapter updates correctly`() {
        val message = ChatMessage(
            senderId = currentUserEmail,
            receiverId = adminEmail,
            message = "Hi Admin",
            timestamp = System.currentTimeMillis()
        )

        val pushRef: DatabaseReference = mock()
        whenever(mockMessagesRef.push()).thenReturn(pushRef)
        whenever(pushRef.setValue(message)).thenReturn(mock())

        // Simulate sending message to Firebase
        mockMessagesRef.push().setValue(message)
        verify(mockMessagesRef).push()
        verify(pushRef).setValue(message)

        // Add message to adapter without notifying RecyclerView
        adapter.addMessage(message, notify = false)

        assertEquals(1, adapter.itemCount)
        assertEquals("Hi Admin", adapter.getItem(0).message)
        assertEquals(currentUserEmail, adapter.getItem(0).senderId)
    }

    @Test
    fun `user receives message from admin and adapter shows it`() {
        val incomingMessage = ChatMessage(
            senderId = adminEmail,
            receiverId = currentUserEmail,
            message = "Hello User",
            timestamp = System.currentTimeMillis()
        )

        // Add message to adapter without notifying RecyclerView
        adapter.addMessage(incomingMessage, notify = false)

        assertEquals(1, adapter.itemCount)
        assertEquals("Hello User", adapter.getItem(0).message)
        assertEquals(adminEmail, adapter.getItem(0).senderId)
    }
}
