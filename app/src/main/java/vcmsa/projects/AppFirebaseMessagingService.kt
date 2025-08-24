package vcmsa.projects.fkj_consultants.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import vcmsa.projects.fkj_consultants.R

class AppFirebaseMessagingService() : FirebaseMessagingService(), Parcelable {

    constructor(parcel: Parcel) : this()

    // 🔔 Handle incoming push notification
    override fun onMessageReceived(msg: RemoteMessage) {
        val title = msg.notification?.title ?: "New message"
        val body = msg.notification?.body ?: ""
        val channelId = "chat_messages"

        // Create notification channel (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                channelId,
                "Chat Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }

        // Build notification
        val n = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_message) // ✅ make sure you have this drawable
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        // Show notification
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), n)
    }

    // 🔑 Handle new FCM token
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(uid)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d("FCM", "✅ Token updated for user: $uid")
                }
                .addOnFailureListener { e ->
                    Log.e("FCM", "❌ Error updating token", e)
                }
        } else {
            Log.w("FCM", "⚠️ No logged-in user, token not saved yet")
        }
    }

    // ---- Parcelable stubs (not actually needed) ----
    override fun writeToParcel(parcel: Parcel, flags: Int) {}
    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<AppFirebaseMessagingService> {
        override fun createFromParcel(parcel: Parcel): AppFirebaseMessagingService {
            return AppFirebaseMessagingService(parcel)
        }

        override fun newArray(size: Int): Array<AppFirebaseMessagingService?> {
            return arrayOfNulls(size)
        }
    }
}
