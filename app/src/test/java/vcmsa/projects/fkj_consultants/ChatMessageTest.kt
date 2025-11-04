package vcmsa.projects.fkj_consultants.models

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ChatMessage model
 *
 * Tests cover:
 * - Default constructor initialization
 * - Field validation
 * - Attachment handling
 * - Message creation scenarios
 * - Edge cases
 */
class ChatMessageTest {

    private lateinit var basicMessage: ChatMessage
    private lateinit var messageWithAttachment: ChatMessage

    @Before
    fun setup() {
        basicMessage = ChatMessage(
            senderId = "user@example.com",
            receiverId = "admin@example.com",
            message = "Hello, I need help with my quotation",
            timestamp = 1699900000000L
        )

        messageWithAttachment = ChatMessage(
            senderId = "user@example.com",
            receiverId = "admin@example.com",
            message = "📄 Quotation #Q001",
            timestamp = 1699900100000L,
            attachmentUri = "/storage/quotation_001.txt"
        )
    }

    @Test
    fun `test default constructor initializes all fields`() {
        val message = ChatMessage()
        assertEquals("", message.senderId)
        assertEquals("", message.receiverId)
        assertEquals("", message.message)
        assertEquals(0L, message.timestamp)
        assertNull(message.attachmentUri)
    }

    @Test
    fun `test basic message creation`() {
        assertEquals("user@example.com", basicMessage.senderId)
        assertEquals("admin@example.com", basicMessage.receiverId)
        assertEquals("Hello, I need help with my quotation", basicMessage.message)
        assertEquals(1699900000000L, basicMessage.timestamp)
        assertNull(basicMessage.attachmentUri)
    }

    @Test
    fun `test message with attachment`() {
        assertEquals("user@example.com", messageWithAttachment.senderId)
        assertEquals("admin@example.com", messageWithAttachment.receiverId)
        assertEquals("📄 Quotation #Q001", messageWithAttachment.message)
        assertEquals(1699900100000L, messageWithAttachment.timestamp)
        assertEquals("/storage/quotation_001.txt", messageWithAttachment.attachmentUri)
    }

    @Test
    fun `test message without attachment has null attachmentUri`() {
        assertNull(basicMessage.attachmentUri)
    }

    @Test
    fun `test message with empty string attachment`() {
        val message = ChatMessage(
            senderId = "user@test.com",
            receiverId = "admin@test.com",
            message = "Test",
            timestamp = System.currentTimeMillis(),
            attachmentUri = ""
        )
        assertEquals("", message.attachmentUri)
        assertNotNull(message.attachmentUri)
    }

    @Test
    fun `test message with quotation text content as attachment`() {
        val quotationText = buildString {
            append("========== QUOTATION ==========\n\n")
            repeat(20) { i ->  // repeat multiple items to increase length
                append("Item $i: Description of product/service $i\n")
                append("Quantity: ${i + 1}\n")
                append("Price: R${(i + 1) * 75}.00\n\n")
            }
            append("Quotation #: Q001\n")
            append("Company: Test Corp\n")
            append("Total: R1500.00")
        }

        val message = ChatMessage(
            senderId = "user@example.com",
            receiverId = "admins",
            message = "📄 Quotation #Q001 - Test Corp",
            timestamp = System.currentTimeMillis(),
            attachmentUri = quotationText
        )

        // Checks that the attachment contains "QUOTATION"
        assertTrue(message.attachmentUri!!.contains("QUOTATION"))

        // Checks that the attachment length is over 500 characters
        assertTrue(message.attachmentUri!!.length > 500)
    }
// (Gideon, 2023).

    @Test
    fun `test message timestamp ordering`() {
        val message1 = ChatMessage(
            senderId = "user1",
            receiverId = "admin",
            message = "First",
            timestamp = 1000L
        )

        val message2 = ChatMessage(
            senderId = "user1",
            receiverId = "admin",
            message = "Second",
            timestamp = 2000L
        )

        assertTrue(message1.timestamp < message2.timestamp)
    }

    @Test
    fun `test message with various attachment types`() {
        val pdfMessage = ChatMessage(
            senderId = "user@test.com",
            receiverId = "admin@test.com",
            message = "📄 PDF Document",
            timestamp = System.currentTimeMillis(),
            attachmentUri = "/storage/document.pdf"
        )
        assertTrue(pdfMessage.attachmentUri!!.endsWith(".pdf"))

        val imageMessage = ChatMessage(
            senderId = "user@test.com",
            receiverId = "admin@test.com",
            message = "🖼️ Image",
            timestamp = System.currentTimeMillis(),
            attachmentUri = "/storage/image.jpg"
        )
        assertTrue(imageMessage.attachmentUri!!.endsWith(".jpg"))
    }

    @Test
    fun `test message equality`() {
        val message1 = ChatMessage(
            senderId = "user@test.com",
            receiverId = "admin@test.com",
            message = "Test",
            timestamp = 1000L
        )

        val message2 = ChatMessage(
            senderId = "user@test.com",
            receiverId = "admin@test.com",
            message = "Test",
            timestamp = 1000L
        )

        assertEquals(message1.senderId, message2.senderId)
        assertEquals(message1.receiverId, message2.receiverId)
        assertEquals(message1.message, message2.message)
        assertEquals(message1.timestamp, message2.timestamp)
    }

    @Test
    fun `test admin receiver format`() {
        val message = ChatMessage(
            senderId = "user@example.com",
            receiverId = "admins",
            message = "Help needed",
            timestamp = System.currentTimeMillis()
        )

        assertEquals("admins", message.receiverId)
    }

    @Test
    fun `test message with special characters`() {
        val message = ChatMessage(
            senderId = "user@example.com",
            receiverId = "admin@example.com",
            message = "Test message with special chars: @#$%^&*()",
            timestamp = System.currentTimeMillis()
        )

        assertTrue(message.message.contains("@#$%^&*()"))
    }

    @Test
    fun `test empty message string`() {
        val message = ChatMessage(
            senderId = "user@test.com",
            receiverId = "admin@test.com",
            message = "",
            timestamp = System.currentTimeMillis(),
            attachmentUri = "/storage/file.pdf"
        )

        assertEquals("", message.message)
        assertNotNull(message.attachmentUri)
    }

    @Test
    fun `test message with unicode characters`() {
        val message = ChatMessage(
            senderId = "user@test.com",
            receiverId = "admin@test.com",
            message = "Hello 👋 Testing emoji 📄 and symbols ✓",
            timestamp = System.currentTimeMillis()
        )

        assertTrue(message.message.contains("👋"))
        assertTrue(message.message.contains("📄"))
        assertTrue(message.message.contains("✓"))
    }
}