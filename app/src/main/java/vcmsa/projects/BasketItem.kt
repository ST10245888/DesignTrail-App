package vcmsa.projects.fkj_consultants.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BasketItem(
    val product: Product = Product(),
    var quantity: Int = 0,
    val selectedColor: String? = null,
    val selectedSize: String? = null,
    var firebaseKey: String? = null
) : Parcelable
