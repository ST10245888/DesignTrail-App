package vcmsa.projects.fkj_consultants

import vcmsa.projects.fkj_consultants.models.BasketItem
import vcmsa.projects.fkj_consultants.models.MaterialItem
import vcmsa.projects.fkj_consultants.models.Quotation
import org.junit.Assert.*
import org.junit.Test

class QuotationGeneratorUnitTest {

    @Test
    fun `quotation total is calculated correctly`() {
        val material1 = MaterialItem(id="1", name="Material A", price=100.0)
        val material2 = MaterialItem(id="2", name="Material B", price=50.0)

        val basket = listOf(
            BasketItem(material = material1, quantity = 2),
            BasketItem(material = material2, quantity = 3)
        )

        val quotation = Quotation(
            quotationId = "q1",
            companyName = "Test Company",
            requesterName = "John Doe",
            products = basket
        )

        val expectedTotal = (2 * 100.0) + (3 * 50.0)
        assertEquals(expectedTotal, quotation.products.sumOf { it.material.price * it.quantity }, 0.0)
    }

    @Test
    fun `quotation created from basket has correct items`() {
        val material = MaterialItem(id="1", name="Material A", price=100.0)
        val basketItem = BasketItem(material = material, quantity = 1)

        val quotation = Quotation(
            quotationId = "q2",
            companyName = "Test Co",
            requesterName = "Alice",
            products = listOf(basketItem)
        )

        assertEquals(1, quotation.products.size)
        assertEquals("Material A", quotation.products[0].material.name)
        assertEquals(1, quotation.products[0].quantity)
    }
}
