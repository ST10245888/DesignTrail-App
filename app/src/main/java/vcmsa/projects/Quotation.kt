package vcmsa.projects.fkj_consultants.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

/**
 * Comprehensive data class representing a Quotation.
 *
 * Designed for use with both local storage and Firebase Realtime Database / Firestore.
 * Supports optional nested items, attached design files, and admin management features.
 */
@Parcelize
data class Quotation(
    // Core Identification
    var id: String = "",                 // Unique identifier for quotation
    var userId: String = "",             // Firebase UID of the user

    // Company & Contact Information
    var companyName: String = "",        // Name of the company associated with quotation
    var customerName: String = "",       // Name of the customer contact
    var customerEmail: String = "",      // Email of the customer contact
    var address: String = "",            // Company or billing address
    var email: String = "",              // Contact email (legacy - prefer customerEmail)
    var phone: String = "",              // Contact phone
    var billTo: String = "",             // Billing information

    // File Management
    var fileName: String = "",           // Name of the quotation file (e.g., quotation_1234.txt)
    var filePath: String = "",           // Local path to quotation file
    var pdfUrl: String = "",             // URL to PDF version of quotation
    var designFileUrl: String? = null,   // Firebase Storage URL for uploaded design
    var downloadUrl: String? = null,     // Public download URL from Firebase Storage
    var fileUrl: String? = null,         // Alternative or legacy remote URL

    // Quotation Details
    var timestamp: Long = System.currentTimeMillis(), // Unix timestamp of quotation creation
    var lastUpdated: Long = System.currentTimeMillis(), // Last modification timestamp
    var status: String = STATUS_PENDING, // Status: Pending, Approved, Rejected, Completed
    var type: String = "Regular",        // Type of quotation (e.g., Regular, Custom)
    var serviceType: String? = null,     // Service category or design type
    var quantity: Int? = null,           // Number of items requested
    var color: String? = null,           // Selected color option
    var notes: String? = null,           // Any extra notes from the user

    // Admin Management
    var adminNotes: String = "",         // Notes from administrator
    var adminId: String = "",            // Admin user ID who processed the quotation
    var approvedAt: Long = 0L,           // Timestamp when approved
    var rejectedAt: Long = 0L,           // Timestamp when rejected

    // Itemized Details
    var items: List<QuotationItem> = emptyList() // List of itemized quotation details
) : Parcelable {

    // Calculated Properties
    val subtotal: Double
        get() = items.sumOf { it.total }

    val formattedDate: String
        get() = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))

    // Status Helper Properties
    val isPending: Boolean
        get() = status == STATUS_PENDING

    val isApproved: Boolean
        get() = status == STATUS_APPROVED

    val isRejected: Boolean
        get() = status == STATUS_REJECTED

    /** Helper to calculate formatted total price */
    fun totalPrice(): Double = subtotal

    /** Returns a short display label for UI lists */
    fun displayTitle(): String = "${companyName.ifEmpty { "Unknown" }} - ${serviceType ?: "N/A"}"

    /** Returns appropriate color resource for status display */
    fun getStatusColorResource(): Int {
        return when (status) {
            STATUS_APPROVED -> android.R.color.holo_green_light
            STATUS_REJECTED -> android.R.color.holo_red_light
            else -> android.R.color.holo_orange_light
        }
    }

    companion object {
        // Status Constants
        const val STATUS_PENDING = "Pending"
        const val STATUS_APPROVED = "Approved"
        const val STATUS_REJECTED = "Rejected"

        /** Validates if a status string is recognized */
        fun isValidStatus(status: String): Boolean {
            return status in listOf(STATUS_PENDING, STATUS_APPROVED, STATUS_REJECTED)
        }

        /** Returns list of all valid status values */
        fun getStatusList(): List<String> {
            return listOf(STATUS_PENDING, STATUS_APPROVED, STATUS_REJECTED)
        }
    }
}