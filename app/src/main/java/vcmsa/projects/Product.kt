package vcmsa.projects.fkj_consultants.models

data class Product(
    var productId: String = "",
    var name: String = "",
    var description: String = "",
    var quantity: Int = 0,
    var color: String = "",
    var size: String = "",
    var price: Double = 0.0,
    var category: String = "",
    var imageUrl: String = "",
    var availability: String = "In Stock",
    var timestamp: Long = 0L
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", 0, "", "", 0.0, "", "", "In Stock", 0L)
}
