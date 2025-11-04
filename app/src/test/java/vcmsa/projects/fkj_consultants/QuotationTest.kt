package vcmsa.projects.fkj_consultants

import org.junit.Before
import vcmsa.projects.fkj_consultants.models.Quotation
import vcmsa.projects.fkj_consultants.models.QuotationItem
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Unit tests for Quotation model
 *
 * Tests cover:
 * - Subtotal calculation from items
 * - Total price calculation
 * - Display title generation
 * - Status and type handling
 * - Optional fields validation
 * - Edge cases with empty or multiple items
 */
class QuotationTest {

    private lateinit var basicQuotation: Quotation
    private lateinit var quotationWithItems: Quotation
    private lateinit var fullQuotation: Quotation

    @Before
    fun setup() {
        basicQuotation = Quotation(
            id = "Q001",
            userId = "U001",
            companyName = "ABC Corp",
            email = "contact@abc.com",
            phone = "0123456789",
            address = "123 Main St",
            billTo = "ABC Corp Finance",
            fileName = "quotation_001.txt",
            filePath = "/data/quotations/quotation_001.txt",
            timestamp = System.currentTimeMillis(),
            status = "Pending"
        )

        val items = listOf(
            QuotationItem("P001", "Logo Design", 150.50, 2),
            QuotationItem("P002", "Business Card", 5.99, 100),
            QuotationItem("P003", "Letterhead", 10.00, 50)
        )

        quotationWithItems = Quotation(
            id = "Q002",
            userId = "U002",
            companyName = "XYZ Ltd",
            email = "info@xyz.com",
            phone = "9876543210",
            address = "456 Oak Ave",
            billTo = "XYZ Ltd Accounting",
            fileName = "quotation_002.txt",
            filePath = "/data/quotations/quotation_002.txt",
            timestamp = System.currentTimeMillis(),
            status = "Approved",
            items = items
        )

        fullQuotation = Quotation(
            id = "Q003",
            userId = "U003",
            companyName = "Full Services Inc",
            email = "services@full.com",
            phone = "1112223333",
            address = "789 Elm Rd",
            billTo = "Full Services Inc",
            fileName = "quotation_003.txt",
            filePath = "/data/quotations/quotation_003.txt",
            timestamp = System.currentTimeMillis(),
            status = "Completed",
            type = "Custom",
            serviceType = "Branding Package",
            quantity = 500,
            color = "Green",
            notes = "Rush order required",
            designFileUrl = "https://storage.firebase.com/designs/d003.pdf",
            downloadUrl = "https://storage.firebase.com/download/d003.pdf",
            items = listOf(
                QuotationItem("P004", "Complete Branding", 5000.00, 1)
            )
        )
    }

    @Test
    fun `test subtotal calculation with no items`() {
        assertEquals(0.0, basicQuotation.subtotal, 0.01)
    }

    @Test
    fun `test subtotal calculation with multiple items`() {
        val expected = 301.0 + 599.0 + 500.0 // 1400.0
        assertEquals(expected, quotationWithItems.subtotal, 0.01)
    }

    @Test
    fun `test subtotal calculation with single item`() {
        assertEquals(5000.0, fullQuotation.subtotal, 0.01)
    }

    @Test
    fun `test totalPrice method matches subtotal`() {
        assertEquals(quotationWithItems.subtotal, quotationWithItems.totalPrice(), 0.01)
    }

    @Test
    fun `test displayTitle with company name and service type`() {
        val title = fullQuotation.displayTitle()
        assertEquals("Full Services Inc - Branding Package", title)
    }

    @Test
    fun `test displayTitle with no service type`() {
        val title = basicQuotation.displayTitle()
        assertEquals("ABC Corp - N/A", title)
    }

    @Test
    fun `test displayTitle with empty company name`() {
        val quotation = Quotation(
            id = "Q004",
            userId = "U004",
            companyName = "",
            serviceType = "Web Design"
        )
        val title = quotation.displayTitle()
        assertEquals("Unknown - Web Design", title)
    }

    @Test
    fun `test displayTitle with empty company and no service`() {
        val quotation = Quotation(id = "Q005", userId = "U005", companyName = "")
        val title = quotation.displayTitle()
        assertEquals("Unknown - N/A", title)
    }

    @Test
    fun `test default status is Pending`() {
        val quotation = Quotation()
        assertEquals("Pending", quotation.status)
    }

    @Test
    fun `test default type is Regular`() {
        val quotation = Quotation()
        assertEquals("Regular", quotation.type)
    }

    @Test
    fun `test status values`() {
        assertEquals("Pending", basicQuotation.status)
        assertEquals("Approved", quotationWithItems.status)
        assertEquals("Completed", fullQuotation.status)
    }

    @Test
    fun `test optional fields in full quotation`() {
        assertEquals("Custom", fullQuotation.type)
        assertEquals("Branding Package", fullQuotation.serviceType)
        assertEquals(500, fullQuotation.quantity)
        assertEquals("Green", fullQuotation.color)
        assertEquals("Rush order required", fullQuotation.notes)
        assertEquals("https://storage.firebase.com/designs/d003.pdf", fullQuotation.designFileUrl)
        assertEquals("https://storage.firebase.com/download/d003.pdf", fullQuotation.downloadUrl)
    }

    @Test
    fun `test optional fields default to null`() {
        assertNull(basicQuotation.serviceType)
        assertNull(basicQuotation.quantity)
        assertNull(basicQuotation.color)
        assertNull(basicQuotation.notes)
        assertNull(basicQuotation.designFileUrl)
        assertNull(basicQuotation.downloadUrl)
        assertNull(basicQuotation.fileUrl)
    }

    @Test
    fun `test empty items list by default`() {
        assertTrue(basicQuotation.items.isEmpty())
    }

    @Test
    fun `test items list size`() {
        assertEquals(3, quotationWithItems.items.size)
        assertEquals(1, fullQuotation.items.size)
    }

    @Test
    fun `test default constructor initializes all fields`() {
        val quotation = Quotation()
        assertEquals("", quotation.id)
        assertEquals("", quotation.userId)
        assertEquals("", quotation.companyName)
        assertEquals("", quotation.address)
        assertEquals("", quotation.email)
        assertEquals("", quotation.phone)
        assertEquals("", quotation.billTo)
        assertEquals("", quotation.fileName)
        assertEquals("", quotation.filePath)
        assertEquals(0L, quotation.timestamp)
        assertEquals("Pending", quotation.status)
        assertEquals("Regular", quotation.type)
        assertTrue(quotation.items.isEmpty())
    }

    @Test
    fun `test quotation with complex item calculations`() {
        val items = listOf(
            QuotationItem("P001", "Item 1", 10.333, 3),   // 31.00
            QuotationItem("P002", "Item 2", 5.555, 2),    // 11.11
            QuotationItem("P003", "Item 3", 100.00, 1)    // 100.00
        )
        val quotation = Quotation(
            id = "Q006",
            userId = "U006",
            companyName = "Test Corp",
            items = items
        )
        val expected = 31.00 + 11.11 + 100.00 // 142.11
        assertEquals(expected, quotation.subtotal, 0.01)
    }

    @Test
    fun `test quotation subtotal with zero-price items`() {
        val items = listOf(
            QuotationItem("P001", "Free Item", 0.0, 10),
            QuotationItem("P002", "Paid Item", 50.0, 2)
        )
        val quotation = Quotation(
            id = "Q007",
            userId = "U007",
            companyName = "Mixed Corp",
            items = items
        )
        assertEquals(100.0, quotation.subtotal, 0.01)
    }

    @Test
    fun `test quotation with large subtotal`() {
        val items = listOf(
            QuotationItem("P001", "Expensive Service", 99999.99, 10)
        )
        val quotation = Quotation(
            id = "Q008",
            userId = "U008",
            companyName = "Premium Corp",
            items = items
        )
        val expected = 999999.90
        assertEquals(expected, quotation.subtotal, 0.01)
    }

    @Test
    fun `test timestamp is set correctly`() {
        val now = System.currentTimeMillis()
        val quotation = Quotation(
            id = "Q009",
            userId = "U009",
            companyName = "Time Corp",
            timestamp = now
        )
        assertEquals(now, quotation.timestamp)
    }

    @Test
    fun `test file paths and names`() {
        assertEquals("quotation_001.txt", basicQuotation.fileName)
        assertEquals("/data/quotations/quotation_001.txt", basicQuotation.filePath)
    }

    @Test
    fun `test user and company identification`() {
        assertEquals("U001", basicQuotation.userId)
        assertEquals("ABC Corp", basicQuotation.companyName)
    }

    @Test
    fun `test contact information`() {
        assertEquals("contact@abc.com", basicQuotation.email)
        assertEquals("0123456789", basicQuotation.phone)
        assertEquals("123 Main St", basicQuotation.address)
        assertEquals("ABC Corp Finance", basicQuotation.billTo)
    }
}
// (Gideon, 2023).