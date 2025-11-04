package vcmsa.projects.models

// (Martin, 2017).
data class Product(
    var productId: String = "",// (Martin, 2017).
    var name: String = "",
    var description: String = "",
    var quantity: Int = 0,
    var color: String = "",// (JetBrains, 2023).
    var size: String = "",
    var price: Double = 0.0,
    var category: String = "",
    var imageUrl: String = "",
    var availability: String = "",
    var timestamp: Long = 0L,
    var customOptions: List<String> = listOf(), //(Google Developers, 2024).
    var id: Any = Any()
)




/**
 * References
 *
 * Google Developers.2024. Cloud Firestore documentation. Available at: https://firebase.google.com/docs/firestore (Accessed: 4 November 2025).
 *
 * JetBrains .2023. Data classes in Kotlin. Available at: https://kotlinlang.org/docs/data-classes.html (Accessed: 4 November 2025).
 *
 * Martin, R.C. .2017. Clean Architecture: A Craftsman’s Guide to Software Structure and Design. Boston: Prentice Hall.
 */
