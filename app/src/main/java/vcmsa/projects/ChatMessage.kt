package vcmsa.projects.fkj_consultants.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatMessage(
    var id: String = "",
    var senderId: String = "",
    var receiverId: String = "",
    var text: String = "",
    var timestamp: Long = 0L
) : Parcelable
