package vcmsa.projects.fkj_consultants.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Quotation(
    val quotationId: String = "",
    val companyName: String = "",
    val requesterName: String = "",
    val email: String = "",
    val tel: String = "",
    val address: String = "",
    val billTo: String = "",
    val total: Double = 0.0,
    val status: String = "Pending",
    val products: List<BasketItem> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable
