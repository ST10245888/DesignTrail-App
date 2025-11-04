package vcmsa.projects.fkj_consultants

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import vcmsa.projects.fkj_consultants.models.Quotation
import vcmsa.projects.fkj_consultants.models.QuotationItem

/**
 * Unit tests for Quotation filtering and sorting logic
 */
class QuotationFilterAndSortTest {

    private lateinit var testQuotations: MutableList<Quotation>

    @Before
    fun setup() {
        testQuotations = mutableListOf(
            createQuotation(
                id = "1",
                companyName = "Acme Corp",
                email = "acme@example.com",
                status = Quotation.STATUS_PENDING,
                subtotal = 1500.0,
                timestamp = 1609459200000L // 2021-01-01
            ),
            createQuotation(
                id = "2",
                companyName = "Beta Ltd",
                email = "beta@example.com",
                status = Quotation.STATUS_APPROVED,
                subtotal = 2500.0,
                timestamp = 1612137600000L // 2021-02-01
            ),
            createQuotation(
                id = "3",
                companyName = "Gamma Inc",
                email = "gamma@example.com",
                status = Quotation.STATUS_REJECTED,
                subtotal = 500.0,
                timestamp = 1614556800000L // 2021-03-01
            ),
            createQuotation(
                id = "4",
                companyName = "Delta Co",
                email = "delta@example.com",
                status = Quotation.STATUS_PENDING,
                subtotal = 3000.0,
                timestamp = 1617235200000L // 2021-04-01
            ),
            createQuotation(
                id = "5",
                companyName = "Epsilon LLC",
                email = "epsilon@example.com",
                status = Quotation.STATUS_APPROVED,
                subtotal = 1000.0,
                timestamp = 1619827200000L // 2021-05-01
            )
        )
    }

    private fun createQuotation(
        id: String,
        companyName: String,
        email: String,
        status: String,
        subtotal: Double,
        timestamp: Long,
        serviceType: String = "General Service",
        phone: String = "0123456789"
    ): Quotation {
        val quotation = Quotation()
        quotation.id = id
        quotation.companyName = companyName
        quotation.email = email
        quotation.status = status
        quotation.timestamp = timestamp
        quotation.serviceType = serviceType
        quotation.phone = phone

        // Corrected: pricePerUnit=subtotal, quantity=1
        quotation.items = listOf(
            QuotationItem(
                productId = "P$id",
                name = "Product $id",
                pricePerUnit = subtotal,
                quantity = 1
            )
        )

        return quotation
    }

    // ========== Filter Tests ==========

    @Test
    fun `filter by status - All should return all quotations`() {
        val filtered = filterByStatus(testQuotations, "All")
        assertEquals(5, filtered.size)
    }

    @Test
    fun `filter by status - Pending should return only pending quotations`() {
        val filtered = filterByStatus(testQuotations, Quotation.STATUS_PENDING)
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.status == Quotation.STATUS_PENDING })
    }

    @Test
    fun `filter by status - Approved should return only approved quotations`() {
        val filtered = filterByStatus(testQuotations, Quotation.STATUS_APPROVED)
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.status == Quotation.STATUS_APPROVED })
    }

    @Test
    fun `filter by status - Rejected should return only rejected quotations`() {
        val filtered = filterByStatus(testQuotations, Quotation.STATUS_REJECTED)
        assertEquals(1, filtered.size)
        assertEquals(Quotation.STATUS_REJECTED, filtered[0].status)
    }

    @Test
    fun `filter by search query - company name match`() {
        val filtered = filterBySearch(testQuotations, "Acme")
        assertEquals(1, filtered.size)
        assertEquals("Acme Corp", filtered[0].companyName)
    }

    @Test
    fun `filter by search query - case insensitive`() {
        val filtered = filterBySearch(testQuotations, "beta")
        assertEquals(1, filtered.size)
        assertEquals("Beta Ltd", filtered[0].companyName)
    }

    @Test
    fun `filter by search query - email match`() {
        val filtered = filterBySearch(testQuotations, "gamma@example.com")
        assertEquals(1, filtered.size)
        assertEquals("Gamma Inc", filtered[0].companyName)
    }

    @Test
    fun `filter by search query - partial match`() {
        val filtered = filterBySearch(testQuotations, "Corp")
        assertEquals(1, filtered.size)
        assertEquals("Acme Corp", filtered[0].companyName)
    }

    @Test
    fun `filter by search query - no match returns empty list`() {
        val filtered = filterBySearch(testQuotations, "NonExistent")
        assertEquals(0, filtered.size)
    }

    @Test
    fun `filter by search query - empty query returns all`() {
        val filtered = filterBySearch(testQuotations, "")
        assertEquals(5, filtered.size)
    }

    @Test
    fun `combined filter - status and search`() {
        val statusFiltered = filterByStatus(testQuotations, Quotation.STATUS_PENDING)
        val searchFiltered = filterBySearch(statusFiltered, "Delta")
        assertEquals(1, searchFiltered.size)
        assertEquals("Delta Co", searchFiltered[0].companyName)
    }

    // ========== Sort Tests ==========

    @Test
    fun `sort by date - newest first`() {
        val sorted = sortQuotations(testQuotations, "Newest First")
        assertEquals("Epsilon LLC", sorted[0].companyName)
        assertEquals("Acme Corp", sorted[4].companyName)
        assertTrue(sorted[0].timestamp > sorted[1].timestamp)
    }

    @Test
    fun `sort by date - oldest first`() {
        val sorted = sortQuotations(testQuotations, "Oldest First")
        assertEquals("Acme Corp", sorted[0].companyName)
        assertEquals("Epsilon LLC", sorted[4].companyName)
        assertTrue(sorted[0].timestamp < sorted[1].timestamp)
    }

    @Test
    fun `sort by company name - A to Z`() {
        val sorted = sortQuotations(testQuotations, "Company (A-Z)")
        assertEquals("Acme Corp", sorted[0].companyName)
        assertEquals("Gamma Inc", sorted[4].companyName)
    }

    @Test
    fun `sort by company name - Z to A`() {
        val sorted = sortQuotations(testQuotations, "Company (Z-A)")
        assertEquals("Gamma Inc", sorted[0].companyName)
        assertEquals("Acme Corp", sorted[4].companyName)
    }

    @Test
    fun `sort by total - high to low`() {
        val sorted = sortQuotations(testQuotations, "Total: High to Low")
        assertEquals(3000.0, sorted[0].subtotal, 0.01)
        assertEquals(500.0, sorted[4].subtotal, 0.01)
        assertTrue(sorted[0].subtotal > sorted[1].subtotal)
    }

    @Test
    fun `sort by total - low to high`() {
        val sorted = sortQuotations(testQuotations, "Total: Low to High")
        assertEquals(500.0, sorted[0].subtotal, 0.01)
        assertEquals(3000.0, sorted[4].subtotal, 0.01)
        assertTrue(sorted[0].subtotal < sorted[1].subtotal)
    }

    // ========== Helper Methods ==========

    private fun filterByStatus(quotations: List<Quotation>, status: String): List<Quotation> {
        return if (status == "All") {
            quotations
        } else {
            quotations.filter { it.status.equals(status, true) }
        }
    }

    private fun filterBySearch(quotations: List<Quotation>, query: String): List<Quotation> {
        if (query.isEmpty()) return quotations
        return quotations.filter {
            it.companyName.contains(query, true) ||
                    it.customerName.contains(query, true) ||
                    it.email.contains(query, true) ||
                    it.serviceType?.contains(query, true) == true ||
                    it.phone.contains(query, true) ||
                    it.id.contains(query, true)
        }
    }

    private fun sortQuotations(quotations: List<Quotation>, sortOption: String): List<Quotation> {
        return when (sortOption) {
            "Newest First", "Date (Newest)" -> quotations.sortedByDescending { it.timestamp }
            "Oldest First", "Date (Oldest)" -> quotations.sortedBy { it.timestamp }
            "Company (A-Z)" -> quotations.sortedBy { it.companyName.lowercase() }
            "Company (Z-A)" -> quotations.sortedByDescending { it.companyName.lowercase() }
            "Total: High to Low", "Total (High-Low)" -> quotations.sortedByDescending { it.subtotal }
            "Total: Low to High", "Total (Low-High)" -> quotations.sortedBy { it.subtotal }
            else -> quotations
        }
    }
}
// (Gideon, 2023).