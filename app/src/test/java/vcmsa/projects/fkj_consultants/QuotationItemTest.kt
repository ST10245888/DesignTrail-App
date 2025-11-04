package vcmsa.projects.fkj_consultants.models

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Unit tests for QuotationItem model
 *
 * Tests cover:
 * - Total calculation with various price/quantity combinations
 * - Rounding behavior for currency calculations
 * - Summary string formatting
 * - Edge cases (zero values, negative values, large numbers)
 */
class QuotationItemTest {

    private lateinit var basicItem: QuotationItem
    private lateinit var itemWithOptionalFields: QuotationItem

    @Before
    fun setup() {
        basicItem = QuotationItem(
            productId = "P001",
            name = "Logo Design",
            pricePerUnit = 150.50,
            quantity = 2
        )

        itemWithOptionalFields = QuotationItem(
            productId = "P002",
            name = "Business Card",
            pricePerUnit = 5.99,
            quantity = 100,
            description = "Premium glossy finish",
            category = "Printing",
            color = "Blue"
        )
    }

    @Test
    fun `test total calculation with basic values`() {
        val expected = 301.0 // 150.50 * 2
        assertEquals(expected, basicItem.total, 0.01)
    }

    @Test
    fun `test total calculation with decimal precision`() {
        val item = QuotationItem(
            productId = "P003",
            name = "Test Item",
            pricePerUnit = 10.333,
            quantity = 3
        )
        val expected = 31.00 // 10.333 * 3 = 30.999, rounded to 31.00
        assertEquals(expected, item.total, 0.01)
    }

    @Test
    fun `test total calculation with rounding up`() {
        val item = QuotationItem(
            productId = "P004",
            name = "Test Item",
            pricePerUnit = 10.555,
            quantity = 1
        )
        val expected = 10.56 // Should round up
        assertEquals(expected, item.total, 0.01)
    }

    @Test
    fun `test total calculation with rounding down`() {
        val item = QuotationItem(
            productId = "P005",
            name = "Test Item",
            pricePerUnit = 10.554,
            quantity = 1
        )
        val expected = 10.55 // Should round down
        assertEquals(expected, item.total, 0.01)
    }

    @Test
    fun `test total with zero quantity`() {
        val item = QuotationItem(
            productId = "P006",
            name = "Test Item",
            pricePerUnit = 100.0,
            quantity = 0
        )
        assertEquals(0.0, item.total, 0.01)
    }

    @Test
    fun `test total with zero price`() {
        val item = QuotationItem(
            productId = "P007",
            name = "Free Item",
            pricePerUnit = 0.0,
            quantity = 5
        )
        assertEquals(0.0, item.total, 0.01)
    }

    @Test
    fun `test total with large quantities`() {
        val item = QuotationItem(
            productId = "P008",
            name = "Bulk Item",
            pricePerUnit = 0.50,
            quantity = 10000
        )
        val expected = 5000.0
        assertEquals(expected, item.total, 0.01)
    }

    @Test
    fun `test total with large price`() {
        val item = QuotationItem(
            productId = "P009",
            name = "Premium Service",
            pricePerUnit = 99999.99,
            quantity = 2
        )
        val expected = 199999.98
        assertEquals(expected, item.total, 0.01)
    }

    @Test
    fun `test summary string format`() {
        val summary = basicItem.summary()
        assertEquals("Logo Design x2 @ R150.50 = R301.00", summary)
    }

    @Test
    fun `test summary with decimal formatting`() {
        val item = QuotationItem(
            productId = "P010",
            name = "Flyer",
            pricePerUnit = 5.5,
            quantity = 10
        )
        val summary = item.summary()
        assertEquals("Flyer x10 @ R5.50 = R55.00", summary)
    }

    @Test
    fun `test summary with item containing optional fields`() {
        val summary = itemWithOptionalFields.summary()
        assertEquals("Business Card x100 @ R5.99 = R599.00", summary)
    }

    @Test
    fun `test optional fields are set correctly`() {
        assertEquals("Premium glossy finish", itemWithOptionalFields.description)
        assertEquals("Printing", itemWithOptionalFields.category)
        assertEquals("Blue", itemWithOptionalFields.color)
    }

    @Test
    fun `test optional fields default to null`() {
        assertNull(basicItem.description)
        assertNull(basicItem.category)
        assertNull(basicItem.color)
    }

    @Test
    fun `test default constructor values`() {
        val emptyItem = QuotationItem()
        assertEquals("", emptyItem.productId)
        assertEquals("", emptyItem.name)
        assertEquals(0.0, emptyItem.pricePerUnit, 0.01)
        assertEquals(0, emptyItem.quantity)
        assertNull(emptyItem.description)
        assertNull(emptyItem.category)
        assertNull(emptyItem.color)
    }

    @Test
    fun `test item with single unit quantity`() {
        val item = QuotationItem(
            productId = "P011",
            name = "Single Service",
            pricePerUnit = 1500.75,
            quantity = 1
        )
        assertEquals(1500.75, item.total, 0.01)
        assertEquals("Single Service x1 @ R1500.75 = R1500.75", item.summary())
    }
}
// (Gideon, 2023).