package vcmsa.projects.fkj_consultants.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    var productId: String = "",
    var name: String = "",
    var color: String = "",
    var size: String = "",
    var price: Double = 0.0,
    var category: String = "",
    var imageUrl: String = "",
    var availability: String = "In Stock",
    var timestamp: Long = 0L
) : Parcelable
