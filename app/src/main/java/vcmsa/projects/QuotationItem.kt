package vcmsa.projects.fkj_consultants.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Data class representing an individual item in a Quotation.
 *
 * This model is used to define each product or service line in a quotation.
 * It supports total computation and can be extended with optional metadata.
 */
@Parcelize
data class QuotationItem(
    var productId: String = "",      // Unique identifier for the product
    var name: String = "",           // Product name or description
    var pricePerUnit: Double = 0.0,  // Price per single unit
    var quantity: Int = 0,           // Quantity ordered
    var description: String? = null, // Optional: extra details (e.g., design notes, material type)
    var category: String? = null,    // Optional: category of the product/service
    var color: String? = null        // Optional: color for design or branding items
) : Parcelable {

    /**
     * Computes the total price for this item (pricePerUnit * quantity).
     * Ensures result is rounded to 2 decimal places for accurate display.
     */
    val total: Double
        get() = BigDecimal(pricePerUnit * quantity)
            .setScale(2, RoundingMode.HALF_UP)
            .toDouble()

    /**
     * Helper function to return a formatted string summary for display.
     */
    fun summary(): String {
        return "$name x$quantity @ R${String.format("%.2f", pricePerUnit)} = R${String.format("%.2f", total)}"
    }
}
// (GeeksForGeeks, 2025).