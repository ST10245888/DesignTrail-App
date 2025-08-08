import android.os.Parcel
import android.os.Parcelable
import vcmsa.projects.fkj_consultants.models.MaterialItem

data class BasketItem(
    val material: MaterialItem,
    val quantity: Int,
    val selectedColor: String?,
    val selectedSize: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(MaterialItem::class.java.classLoader)!!,
        parcel.readInt(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(material, flags)
        parcel.writeInt(quantity)
        parcel.writeString(selectedColor)
        parcel.writeString(selectedSize)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<BasketItem> {
        override fun createFromParcel(parcel: Parcel): BasketItem = BasketItem(parcel)
        override fun newArray(size: Int): Array<BasketItem?> = arrayOfNulls(size)
    }
}
