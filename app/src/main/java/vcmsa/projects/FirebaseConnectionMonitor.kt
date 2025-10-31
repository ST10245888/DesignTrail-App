package vcmsa.projects.fkj_consultants.utils

import com.google.firebase.database.*

object FirebaseConnectionMonitor {

    private val connectionRef: DatabaseReference =
        FirebaseDatabase.getInstance().getReference(".info/connected")

    fun listen(onStatusChange: (Boolean) -> Unit) {
        connectionRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                onStatusChange(connected)
            }

            override fun onCancelled(error: DatabaseError) {
                // Ignore
            }
        })
    }
}
