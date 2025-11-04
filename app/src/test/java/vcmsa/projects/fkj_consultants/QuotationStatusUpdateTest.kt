package vcmsa.projects.fkj_consultants

import vcmsa.projects.fkj_consultants.models.Quotation


import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

/**
 * Unit tests for quotation status updates
 */
class QuotationStatusUpdateTest {

    @Test
    fun `create status update map - approved`() {
        val updates = createStatusUpdateMap(
            status = Quotation.STATUS_APPROVED,
            adminId = "admin123",
            timestamp = 1609459200000L
        )

        assertEquals(Quotation.STATUS_APPROVED, updates["status"])
        assertEquals("admin123", updates["adminId"])
        assertEquals(1609459200000L, updates["lastUpdated"])
        assertEquals(1609459200000L, updates["approvedAt"])
        assertEquals(0L, updates["rejectedAt"])
    }

    @Test
    fun `create status update map - rejected`() {
        val updates = createStatusUpdateMap(
            status = Quotation.STATUS_REJECTED,
            adminId = "admin456",
            timestamp = 1609459200000L
        )

        assertEquals(Quotation.STATUS_REJECTED, updates["status"])
        assertEquals("admin456", updates["adminId"])
        assertEquals(1609459200000L, updates["lastUpdated"])
        assertEquals(1609459200000L, updates["rejectedAt"])
        assertEquals(0L, updates["approvedAt"])
    }

    @Test
    fun `create status update map - pending`() {
        val updates = createStatusUpdateMap(
            status = Quotation.STATUS_PENDING,
            adminId = "admin789",
            timestamp = 1609459200000L
        )

        assertEquals(Quotation.STATUS_PENDING, updates["status"])
        assertEquals("admin789", updates["adminId"])
        assertEquals(1609459200000L, updates["lastUpdated"])
        assertEquals(0L, updates["approvedAt"])
        assertEquals(0L, updates["rejectedAt"])
    }

    private fun createStatusUpdateMap(
        status: String,
        adminId: String,
        timestamp: Long
    ): HashMap<String, Any> {
        val updates = hashMapOf<String, Any>(
            "status" to status,
            "lastUpdated" to timestamp,
            "adminId" to adminId
        )

        when (status) {
            Quotation.STATUS_APPROVED -> {
                updates["approvedAt"] = timestamp
                updates["rejectedAt"] = 0L
            }
            Quotation.STATUS_REJECTED -> {
                updates["rejectedAt"] = timestamp
                updates["approvedAt"] = 0L
            }
            Quotation.STATUS_PENDING -> {
                updates["approvedAt"] = 0L
                updates["rejectedAt"] = 0L
            }
        }

        return updates
    }
}
//(Gideon, 2023).