package vcmsa.projects

data class Order(
    val id: String = "",
    val userId: String = "",
    val itemName: String = "",
    val quantity: Int = 0,
    val description: String = "",
    val title: String = "",
    val status: String = "Pending",
    val timestamp: Long = System.currentTimeMillis()
)