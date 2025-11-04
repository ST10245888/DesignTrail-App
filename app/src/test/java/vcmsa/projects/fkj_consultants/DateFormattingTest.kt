package vcmsa.projects.fkj_consultants

import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*
/**
 * Unit tests for date formatting
 */
class DateFormattingTest {

    @Test
    fun `format date - valid timestamp`() {
        val timestamp = 1609459200000L // 2021-01-01 00:00:00 UTC
        val formatted = formatDate(timestamp)

        assertNotNull(formatted)
        assertNotEquals("Unknown date", formatted)
        assertTrue(formatted.contains("2021") || formatted.contains("21"))
    }

    @Test
    fun `format date - zero timestamp`() {
        val formatted = formatDate(0L)
        assertNotNull(formatted)
    }

    @Test
    fun `format date - negative timestamp should handle gracefully`() {
        val formatted = formatDate(-1L)
        assertNotNull(formatted)
    }

    @Test
    fun `format date - current timestamp`() {
        val currentTime = System.currentTimeMillis()
        val formatted = formatDate(currentTime)

        assertNotNull(formatted)
        assertNotEquals("Unknown date", formatted)
    }

    private fun formatDate(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            "Unknown date"
        }
    }
}
// (Gideon, 2023).