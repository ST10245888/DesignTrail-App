package vcmsa.projects.fkj_consultants.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class representing an individual item in a Quotation.
 *
 * TODO for collaborators:
 * 1. Consider validating productId and name as mandatory fields.
 * 2. Ensure pricePerUnit >= 0 and quantity >= 0 to avoid negative totals.
 * 3. Optionally add unit type (e.g., pcs, kg) for clarity.
 * 4. Extend with optional fields like description, SKU, or category for more detailed quotations.
 * 5. If quantity can be fractional (e.g., meters of material), change type to Double.
 */
@Parcelize
data class QuotationItem(
    var productId: String = "",    // Unique identifier for the product
    var name: String = "",         // Product name
    var pricePerUnit: Double = 0.0, // Price per single unit
    var quantity: Int = 0,         // Quantity ordered
) : Parcelable {

    /**
     * Computes the total price for this item.
     *
     * TODO:
     * 1. Consider adding discount or tax calculations here.
     * 2. Optionally round total to 2 decimal places for display purposes.
     */
    val total: Double
        get() = pricePerUnit * quantity
}
