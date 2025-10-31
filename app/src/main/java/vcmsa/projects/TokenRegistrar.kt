package vcmsa.projects.fkj_consultants.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

object TokenRegistrar {
    private const val TAG = "TokenRegistrar"

    fun registerToken() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                val ref = FirebaseDatabase.getInstance().getReference("users/$uid/fcmTokens")
                // store as key=true
                ref.child(token).setValue(true)
            }
            .addOnFailureListener { e -> Log.w(TAG, "token fetch failed: ${e.message}") }
    }
}
