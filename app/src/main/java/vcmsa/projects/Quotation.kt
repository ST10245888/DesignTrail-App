package vcmsa.projects.fkj_consultants.activities

data class Quotation(
    var id: String = "",            // Firestore document ID
    val userId: String = "",
    val companyName: String = "",
    val requesterName: String = "", // Optional: user's name
    val address: String = "",
    val email: String = "",
    val phone: String = "",
    val billTo: String = "",
    val serviceType: String = "",   // Service requested
    val quantity: Int = 0,          // Number of items
    val color: String = "",         // Selected color
    val notes: String = "",         // Additional notes
    val fileName: String = "",
    val filePath: String = "",
    val subtotal: Double = 0.0,
    val timestamp: Long = 0L,
    val status: String = "Pending"  // Pending, Approved, Rejected
)
