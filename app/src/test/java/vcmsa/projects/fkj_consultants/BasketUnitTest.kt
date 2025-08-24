package vcmsa.projects.fkj_consultants

import vcmsa.projects.fkj_consultants.models.BasketItem
import vcmsa.projects.fkj_consultants.models.MaterialItem
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BasketUnitTest {

    private val material1 = MaterialItem(id="1", name="T-Shirt", price=100.0)
    private val material2 = MaterialItem(id="2", name="Jacket", price=300.0)

    private lateinit var basket: MutableList<BasketItem>

    @Before
    fun setup() {
        basket = mutableListOf()
    }

    @Test
    fun `adding a material to basket increases basket size`() {
        val item = BasketItem(material1, quantity = 2, selectedColor = "Red", selectedSize = "M")
        basket.add(item)

        assertEquals(1, basket.size)
        assertEquals(item, basket[0])
    }

    @Test
    fun `adding same material with different options creates separate entry`() {
        basket.add(BasketItem(material1, quantity = 1, selectedColor = "Red", selectedSize = "M"))
        basket.add(BasketItem(material1, quantity = 1, selectedColor = "Blue", selectedSize = "M"))

        assertEquals(2, basket.size)
        assertEquals("Red", basket[0].selectedColor)
        assertEquals("Blue", basket[1].selectedColor)
    }

    @Test
    fun `removing a material from basket decreases basket size`() {
        val item = BasketItem(material2, quantity = 1)
        basket.add(item)
        basket.remove(item)

        assertEquals(0, basket.size)
    }

    @Test
    fun `updating quantity in basket works correctly`() {
        val item = BasketItem(material1, quantity = 2)
        basket.add(item)

        val updatedItem = item.copy(quantity = 5)
        basket[0] = updatedItem

        assertEquals(5, basket[0].quantity)
    }

    @Test
    fun `basket total is calculated correctly`() {
        basket.add(BasketItem(material1, quantity = 2))
        basket.add(BasketItem(material2, quantity = 1))

        val total = basket.sumOf { it.material.price * it.quantity }
        assertEquals(500.0, total, 0.0)
    }

    @Test
    fun `basket handles empty correctly`() {
        val total = basket.sumOf { it.material.price * it.quantity }
        assertEquals(0.0, total, 0.0)
        assertTrue(basket.isEmpty())
    }

    @Test
    fun `basket item color and size are stored correctly`() {
        val item = BasketItem(material1, quantity = 1, selectedColor = "Green", selectedSize = "L")
        basket.add(item)

        val storedItem = basket[0]
        assertEquals("Green", storedItem.selectedColor)
        assertEquals("L", storedItem.selectedSize)
    }
}
