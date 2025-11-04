package vcmsa.projects.fkj_consultants
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import vcmsa.projects.fkj_consultants.models.Quotation
import vcmsa.projects.fkj_consultants.models.QuotationItem
import java.text.SimpleDateFormat
import java.util.*

/**
 * Unit tests for Quotation model calculations
 */
class QuotationModelTest {

    @Test
    fun `subtotal calculation - single item`() {
        val quotation = Quotation()
        quotation.items = listOf(
            QuotationItem.createSimple("Product 1", 100.0, 2) // quantity as Int
        )
        assertEquals(200.0, quotation.subtotal, 0.01)
    }

    @Test
    fun `subtotal calculation - multiple items`() {
        val quotation = Quotation()
        quotation.items = listOf(
            QuotationItem.createSimple("Product 1", 100.0, 2),
            QuotationItem.createSimple("Product 2", 50.0, 3),
            QuotationItem.createSimple("Product 3", 75.0, 1)
        )
        assertEquals(425.0, quotation.subtotal, 0.01)
    }

    @Test
    fun `subtotal calculation - empty items list`() {
        val quotation = Quotation()
        quotation.items = emptyList()
        assertEquals(0.0, quotation.subtotal, 0.01)
    }

    @Test
    fun `subtotal calculation - zero quantity`() {
        val quotation = Quotation()
        quotation.items = listOf(
            QuotationItem("Product 1", 0.toString(), 100.0)
        )
        assertEquals(0.0, quotation.subtotal, 0.01)
    }

    @Test
    fun `subtotal calculation - zero price`() {
        val quotation = Quotation()
        quotation.items = listOf(
            QuotationItem("Product 1", 5.toString(), 0.0)
        )
        assertEquals(0.0, quotation.subtotal, 0.01)
    }

    @Test
    fun `status validation - valid statuses`() {
        assertTrue(Quotation.isValidStatus(Quotation.STATUS_PENDING))
        assertTrue(Quotation.isValidStatus(Quotation.STATUS_APPROVED))
        assertTrue(Quotation.isValidStatus(Quotation.STATUS_REJECTED))
    }

    @Test
    fun `status validation - invalid status`() {
        assertFalse(Quotation.isValidStatus("INVALID"))
        assertFalse(Quotation.isValidStatus(""))
        assertFalse(Quotation.isValidStatus("pending")) // case sensitive
    }

    @Test
    fun `formatted date - valid timestamp`() {
        val quotation = Quotation()
        quotation.timestamp = 1609459200000L

        val formatted = quotation.formattedDate
        assertNotNull(formatted)
        assertFalse(formatted.isEmpty())
    }
}
// (Gideon, 2023).