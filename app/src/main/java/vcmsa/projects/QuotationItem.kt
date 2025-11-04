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
 * Designed for use with both local storage and Firebase Realtime Database / Firestore.
 */
@Parcelize
data class QuotationItem(
    // Core Identification
    var productId: String = "",           // Unique identifier for the product
    var name: String = "",                // Product name or description

    // Pricing and Quantity
    var pricePerUnit: Double = 0.0,       // Price per single unit
    var quantity: Int = 0,                // Quantity ordered

    // Optional Metadata
    var description: String? = null,      // Optional: extra details (e.g., design notes, material type)
    var category: String? = null,         // Optional: category of the product/service
    var color: String? = null,            // Optional: color for design or branding items

    // Additional Fields for Extended Functionality
    var unit: String? = null,             // Optional: unit of measurement (e.g., "pcs", "hours", "meters")
    var discount: Double = 0.0,           // Optional: discount amount or percentage
    var taxRate: Double = 0.0,            // Optional: tax rate percentage
    var sku: String? = null               // Optional: stock keeping unit
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
     * Computes total with discount applied.
     */
    val totalAfterDiscount: Double
        get() {
            val baseTotal = pricePerUnit * quantity
            val discounted = if (discount > 0) baseTotal - discount else baseTotal
            return BigDecimal(discounted.coerceAtLeast(0.0))
                .setScale(2, RoundingMode.HALF_UP)
                .toDouble()
        }

    /**
     * Computes tax amount for this item.
     */
    val taxAmount: Double
        get() = BigDecimal(totalAfterDiscount * (taxRate / 100))
            .setScale(2, RoundingMode.HALF_UP)
            .toDouble()

    /**
     * Computes final total including tax.
     */
    val finalTotal: Double
        get() = BigDecimal(totalAfterDiscount + taxAmount)
            .setScale(2, RoundingMode.HALF_UP)
            .toDouble()

    /**
     * Helper function to return a formatted string summary for display.
     */
    fun summary(): String {
        val baseSummary = "$name x$quantity${unit?.let { " $it" } ?: ""} @ R${"%.2f".format(pricePerUnit)}"

        return if (discount > 0 || taxRate > 0) {
            "$baseSummary = R${"%.2f".format(finalTotal)}"
        } else {
            "$baseSummary = R${"%.2f".format(total)}"
        }
    }

    /**
     * Returns a detailed breakdown of the item calculation.
     */
    fun detailedBreakdown(): String {
        val builder = StringBuilder()
        builder.append("• $name\n")
        builder.append("  Quantity: $quantity${unit?.let { " $it" } ?: ""}\n")
        builder.append("  Unit Price: R${"%.2f".format(pricePerUnit)}\n")
        builder.append("  Subtotal: R${"%.2f".format(total)}")

        if (discount > 0) {
            builder.append("\n  Discount: -R${"%.2f".format(discount)}")
            builder.append("\n  After Discount: R${"%.2f".format(totalAfterDiscount)}")
        }

        if (taxRate > 0) {
            builder.append("\n  Tax (${"%.1f".format(taxRate)}%): +R${"%.2f".format(taxAmount)}")
        }

        builder.append("\n  Total: R${"%.2f".format(finalTotal)}")

        description?.let {
            builder.append("\n  Notes: $it")
        }

        return builder.toString()
    }

    /**
     * Validates if the item has all required fields filled.
     */
    fun isValid(): Boolean {
        return name.isNotBlank() && pricePerUnit >= 0 && quantity >= 0
    }

    /**
     * Creates a copy with the specified quantity.
     */
    fun withQuantity(newQuantity: Int): QuotationItem {
        return this.copy(quantity = newQuantity)
    }

    /**
     * Creates a copy with the specified price.
     */
    fun withPrice(newPrice: Double): QuotationItem {
        return this.copy(pricePerUnit = newPrice)
    }

    companion object {
        /**
         * Creates a sample item for testing or demonstration.
         */
        fun createSample(): QuotationItem {
            return QuotationItem(
                productId = "PROD_${System.currentTimeMillis()}",
                name = "Sample Product",
                pricePerUnit = 99.99,
                quantity = 2,
                description = "This is a sample product description",
                category = "Electronics",
                color = "Black",
                unit = "pcs",
                discount = 10.0,
                taxRate = 15.0,
                sku = "SKU12345"
            )
        }

        /**
         * Creates a simple item with minimal fields.
         */
        fun createSimple(name: String, price: Double, quantity: Int): QuotationItem {
            return QuotationItem(
                name = name,
                pricePerUnit = price,
                quantity = quantity
            )
        }
    }
}