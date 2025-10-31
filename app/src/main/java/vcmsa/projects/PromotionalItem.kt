package vcmsa.projects.fkj_consultants.activities

import vcmsa.projects.fkj_consultants.R

data class PromotionalItem(
    val name: String = "",
    val pricePerUnit: Double = 0.0,
    val iconRes: Int = R.drawable.ic_placeholder,
    val imageUrl: String? = null,
    val quantity: Int = 0,
    val category: String = "",
    val color: String = "",
    val size: String = "",
    val productId: String = "",
    val availability: String = "",
    val stability: Int = 0
)
