package vcmsa.projects.fkj_consultants.activities

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.ChatMessage

class UserDashboardActivity : AppCompatActivity() {

    companion object {
        var instance: UserDashboardActivity? = null
        var unreadMessageCount = 0
        private const val CHANNEL_ID = "chat_notifications"
        private const val CHANNEL_NAME = "Chat Messages"
        private const val TAG = "UserDashboardActivity"

        /**
         * Updates the notification badge when a new message arrives or is cleared.
         */
        fun updateNotificationBadgeStatic(show: Boolean) {
            instance?.runOnUiThread {
                if (show) {
                    unreadMessageCount++
                    instance?.notificationBadge?.visibility = View.VISIBLE
                    instance?.notificationBadge?.text =
                        if (unreadMessageCount > 99) "99+" else unreadMessageCount.toString()
                    instance?.pingAndBlinkBell()
                    Log.d(TAG, "Badge updated: $unreadMessageCount unread message(s).")
                } else {
                    unreadMessageCount = 0
                    instance?.notificationBadge?.visibility = View.GONE
                    instance?.stopBellBlinking()
                    Log.d(TAG, "Badge reset and bell stopped blinking.")
                }
            }
        }
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var notificationBell: ImageView
    lateinit var notificationBadge: TextView

    private var chatRef: DatabaseReference? = null
    private var chatListener: ChildEventListener? = null
    private val handler = Handler(Looper.getMainLooper())
    private var bellBlinking = false
    private var blinkRunnable: Runnable? = null
    private var lastAdminMessageKey: String? = null // Prevent duplicate message triggers

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_dashboard)

        Log.i(TAG, "UserDashboardActivity created.")

        instance = this
        auth = FirebaseAuth.getInstance()
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        notificationBell = findViewById(R.id.notificationBell)
        notificationBadge = findViewById(R.id.notificationBadge)

        setupBottomNavigation()
        setupClickListeners()

        // Check user authentication and redirect if admin or guest
        auth.currentUser?.let { user ->
            Log.d(TAG, "Authenticated user: ${user.email}")
            if (isAdmin(user.email)) {
                Log.i(TAG, "Admin detected. Redirecting to AdminDashboardActivity.")
                startActivity(Intent(this, AdminDashboardActivity::class.java))
                finish()
                return
            }
            listenForAdminReplies(user.uid)
        } ?: run {
            Log.w(TAG, "No user authenticated. Redirecting to LoginActivity.")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        createNotificationChannel()
    }

    private fun isAdmin(email: String?) =
        email != null && LoginActivity.ADMIN_EMAILS.any { it.equals(email, true) }

    /**
     * Starts listening for new messages from the admin in the user's chat thread.
     */
    private fun listenForAdminReplies(userId: String) {
        val adminId = "admin"
        val chatId = if (userId < adminId) "${userId}_$adminId" else "${adminId}_$userId"
        chatRef = FirebaseDatabase.getInstance().getReference("chats/$chatId/messages")
        Log.d(TAG, "Listening for admin replies in chat path: $chatId")

        chatListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val msgKey = snapshot.key ?: return
                val msg = snapshot.getValue(ChatMessage::class.java) ?: return
                Log.d(TAG, "New message snapshot detected with key: $msgKey")

                // Process only new admin messages
                if (msg.senderId == "admin" && msgKey != lastAdminMessageKey) {
                    lastAdminMessageKey = msgKey
                    Log.i(TAG, "New admin message: ${msg.message}")
                    onNewAdminMessage(msg.message)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Chat listener cancelled: ${error.message}")
            }
        }

        chatRef?.addChildEventListener(chatListener as ChildEventListener)
    }

    /**
     * Called whenever the admin sends a new message.
     */
    fun onNewAdminMessage(message: String) {
        Log.i(TAG, "Processing new admin message: ${message.take(40)}")
        updateNotificationBadgeStatic(true)
        showSystemNotification(message)
        Toast.makeText(this, "New message from Admin: ${message.take(40)}", Toast.LENGTH_SHORT).show()
    }

    /**
     * Displays a native Android system notification for a new admin message.
     */
    private fun showSystemNotification(message: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(this, ChatActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("New Message from Admin")
            .setContentText(if (message.length > 40) message.take(40) + "..." else message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        Log.d(TAG, "System notification displayed for new admin message.")
    }

    /**
     * Creates a notification channel (Android O+).
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel '$CHANNEL_NAME' created.")
        }
    }

    /**
     * Animates the bell icon and starts blinking when a new message arrives.
     */
    private fun pingAndBlinkBell() {
        Log.d(TAG, "Bell ping animation triggered.")
        ObjectAnimator.ofFloat(notificationBell, "rotation", 0f, 25f, -25f, 0f).apply {
            duration = 400
            start()
        }
        startBellBlinking()
    }

    /**
     * Starts blinking effect on the bell icon for unread messages.
     */
    private fun startBellBlinking() {
        if (bellBlinking) return
        bellBlinking = true
        blinkRunnable = object : Runnable {
            override fun run() {
                notificationBell.alpha = if (notificationBell.alpha == 1f) 0.4f else 1f
                handler.postDelayed(this, 500)
            }
        }
        handler.post(blinkRunnable!!)
        Log.d(TAG, "Bell blinking started.")
    }

    /**
     * Stops the blinking effect on the bell icon.
     */
    private fun stopBellBlinking() {
        bellBlinking = false
        blinkRunnable?.let { handler.removeCallbacks(it) }
        notificationBell.alpha = 1f
        Log.d(TAG, "Bell blinking stopped.")
    }

    /**
     * Sets up the click listeners for buttons and icons on the dashboard.
     */
    private fun setupClickListeners() {
        findViewById<Button>(R.id.btnCatalog).setOnClickListener {
            Log.d(TAG, "Navigating to CatalogActivity.")
            startActivity(Intent(this, CatalogActivity::class.java))
        }
        findViewById<Button>(R.id.btnQuotations).setOnClickListener {
            Log.d(TAG, "Navigating to QuotationActivity.")
            startActivity(Intent(this, QuotationActivity::class.java))
        }
        findViewById<Button>(R.id.btnMessaging).setOnClickListener {
            Log.d(TAG, "Navigating to ChatActivity via button.")
            openChatActivity()
        }
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            Log.i(TAG, "User logged out.")
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        notificationBell.setOnClickListener { showNotificationPopup() }
    }

    /**
     * Opens the chat activity and resets unread message indicators.
     */
    private fun openChatActivity() {
        Log.d(TAG, "Opening chat activity, resetting badge and bell.")
        unreadMessageCount = 0
        notificationBadge.visibility = View.GONE
        stopBellBlinking()
        startActivity(Intent(this, ChatActivity::class.java))
    }

    /**
     * Displays a popup showing unread message count when the bell icon is tapped.
     */
    private fun showNotificationPopup() {
        val hasNew = unreadMessageCount > 0
        Log.d(TAG, "Notification popup opened. Has new: $hasNew")
        AlertDialog.Builder(this)
            .setTitle(if (hasNew) "New Messages" else "No New Messages")
            .setMessage(if (hasNew) "You have $unreadMessageCount unread message(s). Go to chat?" else "No new messages.")
            .setPositiveButton("Go to Chat") { _, _ -> openChatActivity() }
            .setNegativeButton("Dismiss", null)
            .show()
    }

    /**
     * Configures the bottom navigation for the dashboard.
     */
    private fun setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> true
                R.id.nav_catalog -> { startActivity(Intent(this, CatalogActivity::class.java)); true }
                R.id.nav_dashboard -> true
                R.id.nav_messages -> { startActivity(Intent(this, ChatActivity::class.java)); true }
                R.id.nav_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); true }
                else -> false
            }
        }
        Log.d(TAG, "Bottom navigation initialized.")
    }

    /**
     * Cleans up Firebase listeners and stops blinking when activity is destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()
        chatListener?.let { chatRef?.removeEventListener(it) }
        stopBellBlinking()
        instance = null
        Log.i(TAG, "UserDashboardActivity destroyed and listeners removed.")
    }
}
