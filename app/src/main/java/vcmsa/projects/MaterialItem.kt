package vcmsa.projects.fkj_consultants.models

import android.os.Parcel
import android.os.Parcelable

data class MaterialItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val availableColors: List<String> = emptyList(),
    val availableSizes: List<String> = emptyList(),
    val category: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: emptyList(),
        parcel.createStringArrayList() ?: emptyList(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeDouble(price)
        parcel.writeString(imageUrl)
        parcel.writeStringList(availableColors)
        parcel.writeStringList(availableSizes)
        parcel.writeString(category)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<MaterialItem> {
        override fun createFromParcel(parcel: Parcel): MaterialItem = MaterialItem(parcel)
        override fun newArray(size: Int): Array<MaterialItem?> = arrayOfNulls(size)
    }
}
