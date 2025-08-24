package vcmsa.projects.fkj_consultants.models

import android.os.Parcel
import android.os.Parcelable

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
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readString() ?: "Pending",
        parcel.createTypedArrayList(BasketItem.CREATOR) ?: emptyList(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(quotationId)
        parcel.writeString(companyName)
        parcel.writeString(requesterName)
        parcel.writeString(email)
        parcel.writeString(tel)
        parcel.writeString(address)
        parcel.writeString(billTo)
        parcel.writeDouble(total)
        parcel.writeString(status)
        parcel.writeTypedList(products)
        parcel.writeLong(timestamp)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Quotation> {
        override fun createFromParcel(parcel: Parcel): Quotation = Quotation(parcel)
        override fun newArray(size: Int): Array<Quotation?> = arrayOfNulls(size)
    }
}
