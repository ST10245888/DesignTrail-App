package vcmsa.projects.fkj_consultants

import vcmsa.projects.fkj_consultants.models.MaterialItem
import org.junit.Assert.*
import org.junit.Test

class InventoryManagementUnitTest {

    private val inventory = mutableMapOf<String, Int>()

    @Test
    fun `adding materials to inventory updates stock`() {
        val material = MaterialItem(id="1", name="Material A", price=100.0)
        inventory[material.id] = inventory.getOrDefault(material.id, 0) + 5

        assertEquals(5, inventory[material.id])
        // Add more stock
        inventory[material.id] = inventory.getOrDefault(material.id, 0) + 3
        assertEquals(8, inventory[material.id])
    }

    @Test
    fun `removing materials reduces stock`() {
        val material = MaterialItem(id="2", name="Material B", price=50.0)
        inventory[material.id] = 10
        inventory[material.id] = inventory[material.id]?.minus(4) ?: 0

        assertEquals(6, inventory[material.id])
    }

    @Test
    fun `cannot remove more than available stock`() {
        val material = MaterialItem(id="3", name="Material C", price=75.0)
        inventory[material.id] = 2
        val removeAmount = 5
        val remainingStock = (inventory[material.id] ?: 0) - removeAmount
        inventory[material.id] = remainingStock.coerceAtLeast(0)

        assertEquals(0, inventory[material.id])
    }
}
