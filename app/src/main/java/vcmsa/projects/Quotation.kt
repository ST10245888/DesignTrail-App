package vcmsa.projects.fkj_consultants.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class representing a Quotation.
 *
 * Designed for use with both local storage and Firebase Realtime Database / Firestore.
 * Supports optional nested items and attached design files.
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

    var fileName: String = "",           // Name of the quotation file (e.g., quotation_1234.txt)
    var filePath: String = "",           // Local path to quotation file
    var timestamp: Long = 0L,            // Unix timestamp of quotation creation or upload
    var status: String = "Pending",      // Status: Pending, Approved, Rejected, Completed

    var type: String = "Regular",        // Type of quotation (e.g., Regular, Custom)
    var serviceType: String? = null,     // Service category or design type
    var quantity: Int? = null,           // Number of items requested
    var color: String? = null,           // Selected color option
    var notes: String? = null,           // Any extra notes from the user

    var designFileUrl: String? = null,   // Firebase Storage URL for uploaded design
    var downloadUrl: String? = null,     // Public download URL from Firebase Storage
    var fileUrl: String? = null,         // Alternative or legacy remote URL

    // List of itemized quotation details (optional)
    var items: List<QuotationItem> = emptyList()
) : Parcelable {

    /** Calculates subtotal by summing totals from all quotation items. */
    val subtotal: Double
        get() = items.sumOf { it.total }

    /** Helper to calculate formatted total price */
    fun totalPrice(): Double = subtotal

    /** Returns a short display label for UI lists */
    fun displayTitle(): String = "${companyName.ifEmpty { "Unknown" }} - ${serviceType ?: "N/A"}"
}
// (GeeksForGeeks, 2025).