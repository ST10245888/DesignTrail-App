package vcmsa.projects.fkj_consultants.utils

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Helper functions for managing FCM tokens
 */
object FCMTokenHelper {
    private const val TAG = "FCMTokenHelper"
    private const val PREFS_NAME = "fcm_prefs"
    private const val KEY_PENDING_TOKEN = "pending_token"

    /**
     * Get and save FCM token for current user
     * Call this after successful login
     */
    fun saveFCMTokenForCurrentUser(context: Context) {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            Log.w(TAG, "No user logged in")
            return
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d(TAG, "FCM Token: $token")

            // Save to Firebase Realtime Database
            saveFCMTokenToDatabase(currentUser.uid, token)

            // Check for any pending tokens
            uploadPendingToken(currentUser.uid, context)
        }
    }

    /**
     * Save FCM token to Firebase Realtime Database
     */
    private fun saveFCMTokenToDatabase(userId: String, token: String) {
        val db = FirebaseDatabase.getInstance()
        val updates = hashMapOf<String, Any>(
            "users/$userId/fcmToken" to token,
            "users/$userId/fcmTokenUpdatedAt" to System.currentTimeMillis()
        )

        db.reference.updateChildren(updates)
            .addOnSuccessListener {
                Log.d(TAG, "✅ FCM token saved for user: $userId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to save FCM token", e)
            }
    }

    /**
     * Upload any pending FCM token that was saved before login
     */
    private fun uploadPendingToken(userId: String, context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val pendingToken = prefs.getString(KEY_PENDING_TOKEN, null)

        if (pendingToken != null) {
            Log.d(TAG, "Found pending token, uploading...")
            saveFCMTokenToDatabase(userId, pendingToken)

            // Clear pending token
            prefs.edit().remove(KEY_PENDING_TOKEN).apply()
        }
    }

    /**
     * Delete FCM token on logout
     */
    fun deleteFCMTokenOnLogout() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val db = FirebaseDatabase.getInstance()
            db.getReference("users/${currentUser.uid}/fcmToken")
                .removeValue()
                .addOnSuccessListener {
                    Log.d(TAG, "✅ FCM token removed on logout")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "❌ Failed to remove FCM token", e)
                }
        }

        // Delete FCM token from device
        FirebaseMessaging.getInstance().deleteToken()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "✅ FCM token deleted from device")
                } else {
                    Log.e(TAG, "❌ Failed to delete FCM token", task.exception)
                }
            }
    }

    /**
     * Request notification permission (Android 13+)
     * Call this from your main activity
     */
    fun requestNotificationPermission(activity: android.app.Activity) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.app.ActivityCompat.checkSelfPermission(
                    activity,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                androidx.core.app.ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }
    }
}

/**
 * Usage Examples:
 *
 * 1. After login:
 *    FCMTokenHelper.saveFCMTokenForCurrentUser(this)
 *
 * 2. On logout:
 *    FCMTokenHelper.deleteFCMTokenOnLogout()
 *
 * 3. Request notification permission (in MainActivity onCreate):
 *    FCMTokenHelper.requestNotificationPermission(this)
 */