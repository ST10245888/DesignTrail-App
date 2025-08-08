package vcmsa.projects.fkj_consultants.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MaterialItem(
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var imageUrl: String = "",
    var price: Double = 0.0,
    var availableColors: List<String> = emptyList(),
    var availableSizes: List<String> = emptyList(),
    var category: String = ""
) : Parcelable
