package vcmsa.projects.fkj_consultants

import vcmsa.projects.fkj_consultants.models.BasketItem
import vcmsa.projects.fkj_consultants.models.MaterialItem
import org.junit.Assert.*
import org.junit.Test

class MaterialSelectionUnitTest {

    @Test
    fun `material selection stores selected color and size`() {
        val material = MaterialItem(id="1", name="T-Shirt", price=120.0)
        val basketItem = BasketItem(material, quantity = 2, selectedColor = "Red", selectedSize = "M")

        assertEquals("Red", basketItem.selectedColor)
        assertEquals("M", basketItem.selectedSize)
        assertEquals(2, basketItem.quantity)
        assertEquals("T-Shirt", basketItem.material.name)
    }

    @Test
    fun `updating quantity works correctly`() {
        val material = MaterialItem(id="1", name="Jacket", price=300.0)
        var basketItem = BasketItem(material, quantity = 1)
        basketItem = basketItem.copy(quantity = 4)

        assertEquals(4, basketItem.quantity)
    }
}
