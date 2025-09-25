package vcmsa.projects.fkj_consultants.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import vcmsa.projects.fkj_consultants.models.Conversation

class ChatListViewModel : ViewModel() {

    private val dbRef = FirebaseDatabase.getInstance().reference.child("conversations")
    val conversations = MutableLiveData<List<Conversation>>()
    private val adminId = "Mavuso2@gmail_com"

    fun startAdmin() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val latestMap = mutableMapOf<String, Conversation>()

                snapshot.children.forEach { convoSnapshot ->
                    val convo = convoSnapshot.getValue(Conversation::class.java)
                    if (convo != null && (convo.userA == adminId || convo.userB == adminId)) {
                        val otherUserId = convo.getOtherUserId(adminId)
                        val current = latestMap[otherUserId]
                        if (current == null || convo.lastTimestamp > current.lastTimestamp) {
                            latestMap[otherUserId] = convo
                        }
                    }
                }

                val list = latestMap.values.sortedByDescending { it.lastTimestamp }
                conversations.postValue(list)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}