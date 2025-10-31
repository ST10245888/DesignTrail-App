package vcmsa.projects.fkj_consultants.utils

import android.util.Log
import com.google.firebase.database.*

object UserCleanupUtil {
    private const val TAG = "UserCleanup"

    fun cleanupUserTimestamps(callback: (success: Boolean, message: String) -> Unit) {
        val usersRef = FirebaseDatabase.getInstance().getReference("Users")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var fixedCount = 0
                var errorCount = 0

                for (userSnapshot in snapshot.children) {
                    try {
                        val updates = mutableMapOf<String, Any>()

                        // Fix createdAt if it's a string
                        val createdAtValue = userSnapshot.child("createdAt").value
                        if (createdAtValue is String) {
                            val longValue = createdAtValue.toLongOrNull() ?: System.currentTimeMillis()
                            updates["createdAt"] = longValue
                            fixedCount++
                        } else if (createdAtValue == null) {
                            updates["createdAt"] = System.currentTimeMillis()
                            fixedCount++
                        }

                        // Fix lastLogin if it's a string
                        val lastLoginValue = userSnapshot.child("lastLogin").value
                        if (lastLoginValue is String) {
                            val longValue = lastLoginValue.toLongOrNull() ?: System.currentTimeMillis()
                            updates["lastLogin"] = longValue
                            fixedCount++
                        } else if (lastLoginValue == null) {
                            updates["lastLogin"] = System.currentTimeMillis()
                            fixedCount++
                        }

                        // Apply updates if any
                        if (updates.isNotEmpty()) {
                            userSnapshot.ref.updateChildren(updates)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fixing user ${userSnapshot.key}", e)
                        errorCount++
                    }
                }

                val message = "Fixed $fixedCount user fields. Errors: $errorCount"
                Log.i(TAG, message)
                callback(errorCount == 0, message)
            }

            override fun onCancelled(error: DatabaseError) {
                val message = "User cleanup failed: ${error.message}"
                Log.e(TAG, message, error.toException())
                callback(false, message)
            }
        })
    }
}