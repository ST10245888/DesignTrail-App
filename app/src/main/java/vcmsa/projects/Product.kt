package vcmsa.projects.fkj_consultants.models

data class Product(
    var productId: String = "",
    var name: String = "",
    var description: String = "",   // Added
    var quantity: Int = 0,          // Added
    var color: String = "",
    var size: String = "",
    var price: Double = 0.0,
    var category: String = "",
    var imageUrl: String = "",
    var availability: String = "In Stock",
    var timestamp: Long = 0L
)
