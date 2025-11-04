package vcmsa.projects.fkj_consultants

import org.junit.Before
import vcmsa.projects.fkj_consultants.models.Quotation
import vcmsa.projects.fkj_consultants.models.QuotationItem
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode
/**
 * Integration tests for Quotation and QuotationItem working together
 */
class QuotationIntegrationTest {

    @Test
    fun `test quotation with mixed item types calculates correct total`() {
        val items = listOf(
            QuotationItem("P001", "Service A", 100.50, 1),
            QuotationItem("P002", "Service B", 50.25, 2),
            QuotationItem("P003", "Service C", 75.99, 3)
        )
        val quotation = Quotation(
            id = "Q_INT_001",
            userId = "U_INT_001",
            companyName = "Integration Test Corp",
            items = items
        )

        // Manual calculation: 100.50 + (50.25 * 2) + (75.99 * 3)
        // = 100.50 + 100.50 + 227.97 = 428.97
        val expected = 428.97
        assertEquals(expected, quotation.subtotal, 0.01)
    }

    @Test
    fun `test modifying items updates quotation subtotal`() {
        val items = mutableListOf(
            QuotationItem("P001", "Item 1", 10.0, 1)
        )
        val quotation = Quotation(
            id = "Q_INT_002",
            userId = "U_INT_002",
            companyName = "Mutable Corp",
            items = items
        )

        assertEquals(10.0, quotation.subtotal, 0.01)

        // Add more items
        val newItems = items + QuotationItem("P002", "Item 2", 20.0, 2)
        val updatedQuotation = quotation.copy(items = newItems)

        assertEquals(50.0, updatedQuotation.subtotal, 0.01)
    }

    @Test
    fun `test empty quotation behaves correctly`() {
        val quotation = Quotation()
        assertEquals(0.0, quotation.subtotal, 0.01)
        assertEquals(0.0, quotation.totalPrice(), 0.01)
        assertEquals("Unknown - N/A", quotation.displayTitle())
    }

    @Test
    fun `test quotation with single high-value item`() {
        val item = QuotationItem("P_PREMIUM", "Premium Package", 50000.00, 1)
        val quotation = Quotation(
            id = "Q_PREMIUM",
            userId = "U_PREMIUM",
            companyName = "Premium Client",
            items = listOf(item)
        )

        assertEquals(50000.0, quotation.subtotal, 0.01)
        assertEquals("Premium Package x1 @ R50000.00 = R50000.00", item.summary())
    }
}
// (Gideon, 2023).