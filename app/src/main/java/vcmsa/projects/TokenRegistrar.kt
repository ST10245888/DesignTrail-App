package vcmsa.projects.fkj_consultants.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

object TokenRegistrar { // (Kotlinlang.org, 2024)
    private const val TAG = "TokenRegistrar" // Tag for logging purposes (Android Developers, 2024)

    fun registerToken() { // Method to register the user's FCM token (Google Firebase, 2024)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return 
        FirebaseMessaging.getInstance().token 
            .addOnSuccessListener { token ->
                // Reference to the user’s FCM token path in Firebase Realtime Database (Android Developers, 2024)
                val ref = FirebaseDatabase.getInstance().getReference("users/$uid/fcmTokens") 
              
                ref.child(token).setValue(true)
            }
            .addOnFailureListener { e -> Log.w(TAG, "token fetch failed: ${e.message}") } 
    }
}

/*
Reference List

Android Developers, 2024. Firebase Realtime Database | Android Integration Guide. [online] Available at: <https://firebase.google.com/docs/database/android/start> [Accessed 4 November 2025].

Kotlinlang.org, 2024. Kotlin for Android Developers | Object Declarations and Singletons. [online] Available at: <https://kotlinlang.org/docs/object-declarations.html> [Accessed 4 November 2025].

*/
