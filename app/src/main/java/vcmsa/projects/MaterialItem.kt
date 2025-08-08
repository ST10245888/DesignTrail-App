package vcmsa.projects.fkj_consultants.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MaterialItem(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val price: Double,
    val availableColors: List<String>,
    val availableSizes: List<String>,
    val category: String
) : Parcelable
