package vcmsa.projects.fkj_consultants

import org.junit.Test
import kotlin.test.assertEquals
import org.junit.Assert.assertTrue

/**
 * Unit tests for search query trimming and validation
 */
class SearchQueryTest {

    @Test
    fun `trim search query - removes leading and trailing spaces`() {
        assertEquals("test", "  test  ".trim())
        assertEquals("search query", "  search query  ".trim())
    }

    @Test
    fun `trim search query - handles empty string`() {
        assertEquals("", "".trim())
        assertEquals("", "   ".trim())
    }

    @Test
    fun `search query case insensitive matching`() {
        val query = "ACME"
        val text = "Acme Corp"
        assertTrue(text.contains(query, ignoreCase = true))
    }

    @Test
    fun `search query partial matching`() {
        val query = "Corp"
        val text = "Acme Corporation"
        assertTrue(text.contains(query, ignoreCase = true))
    }
}
//(Gideon, 2023).