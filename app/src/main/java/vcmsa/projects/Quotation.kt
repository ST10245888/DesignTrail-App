package vcmsa.projects.fkj_consultants.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class representing a Quotation.
 *
 * TODO for collaborators:
 * 1. Consider validation for mandatory fields like id, userId, companyName, filePath.
 * 2. Add JSON annotations if using Firestore/Realtime Database with different key names.
 * 3. Optionally add a status enum instead of plain string for type safety.
 * 4. Consider adding createdAt / updatedAt timestamps separate from file timestamp.
 * 5. Implement Parcelable carefully if nested objects (QuotationItem) become complex.
 */
@Parcelize
data class Quotation(
    var id: String = "",                 // Unique identifier for quotation
    var userId: String = "",             // Firebase UID of the user
    var companyName: String = "",        // Name of the company associated with quotation
    var address: String = "",            // Company or billing address
    var email: String = "",              // Contact email
    var phone: String = "",              // Contact phone
    var billTo: String = "",             // Billing information
    var fileName: String = "",           // Name of the quotation file (e.g., PDF)
    var filePath: String = "",           // Local device path to quotation file
    var timestamp: Long = 0L,            // Timestamp of quotation creation or upload
    var status: String = "Pending",      // Quotation status: Pending, Approved, Rejected
    var type: String = "",               // Type of quotation (e.g., regular, custom)
    var serviceType: String? = null,     // Optional: type of service provided
    var quantity: Int? = null,           // Optional: quantity of items
    var color: String? = null,           // Optional: color info for items
    var notes: String? = null,           // Optional: any additional notes
    var designFileUrl: String? = null,   // Optional: URL to uploaded design file

    // ✅ Added for Firebase Storage / download links
    var downloadUrl: String? = null,     // Direct download URL from Firebase Storage
    var fileUrl: String? = null,         // Alternate remote file URL (backup / legacy)

    var items: List<QuotationItem> = emptyList() // List of individual items in quotation
) : Parcelable {

    /**
     * Computes the subtotal by summing the total of each QuotationItem.
     *
     * TODO:
     * 1. Consider adding taxes, discounts, or shipping fees for total calculation.
     * 2. Ensure items list is never null to avoid NullPointerException.
     * 3. Optionally cache subtotal if performance becomes an issue with many items.
     */
    val subtotal: Double
        get() = items.sumOf { it.total }
}
