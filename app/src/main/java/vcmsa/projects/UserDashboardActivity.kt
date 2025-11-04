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
import androidx.core.content.ContextCompat
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

        private val ADMIN_EMAILS = listOf(
            "kush@gmail.com",
            "keitumetse01@gmail.com",
            "malikaOlivia@gmail.com",
            "JamesJameson@gmail.com"
        )

        /**
         * Updates the notification badge when a new message arrives or is cleared.
         */
        fun updateNotificationBadgeStatic(show: Boolean) {
            instance?.runOnUiThread {
                if (show) {
                    unreadMessageCount++
                    instance?.updateNotificationBadge()
                    instance?.pingAndBlinkBell()
                    Log.d(TAG, "Badge updated: $unreadMessageCount unread message(s).")
                } else {
                    unreadMessageCount = 0
                    instance?.updateNotificationBadge()
                    instance?.stopBellBlinking()
                    Log.d(TAG, "Badge reset and bell stopped blinking.")
                }
            }
        }

        fun isAdmin(email: String?): Boolean {
            return email != null && ADMIN_EMAILS.any { it.equals(email, ignoreCase = true) }
        }

        fun encodeEmail(email: String): String = email.replace(".", ",")
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var notificationBell: ImageView
    private lateinit var notificationBadge: TextView
    private lateinit var welcomeText: TextView
    private lateinit var btnCatalog: Button
    private lateinit var btnQuotations: Button
    private lateinit var btnMessaging: Button
    private lateinit var btnLogout: Button

    private var chatRef: DatabaseReference? = null
    private var chatListener: ChildEventListener? = null
    private val handler = Handler(Looper.getMainLooper())
    private var bellBlinking = false
    private var blinkRunnable: Runnable? = null
    private val processedMessageKeys = mutableSetOf<String>()

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_dashboard)

        Log.i(TAG, "═══════════════════════════════════")
        Log.i(TAG, "👤 USER DASHBOARD INITIALIZED")
        Log.i(TAG, "═══════════════════════════════════")

        instance = this
        initializeViews()
        setupBottomNavigation()
        setupClickListeners()

        // Check user authentication and redirect if admin or guest
        auth.currentUser?.let { user ->
            Log.d(TAG, "Authenticated user: ${user.email}")
            if (isAdmin(user.email)) {
                Log.i(TAG, "Admin detected. Redirecting to AdminDashboardActivity")
                startActivity(Intent(this, AdminDashboardActivity::class.java))
                finish()
                return
            }

            // Set welcome message
            welcomeText.text = "Welcome, ${user.email?.split("@")?.first() ?: "User"}!"

            listenForAdminReplies(user.email!!)
        } ?: run {
            Log.w(TAG, "No user authenticated. Redirecting to LoginActivity")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        createNotificationChannel()
    }

    private fun initializeViews() {
        auth = FirebaseAuth.getInstance()
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        notificationBell = findViewById(R.id.notificationBell)
        notificationBadge = findViewById(R.id.notificationBadge)
        welcomeText = findViewById(R.id.welcomeText)
        btnCatalog = findViewById(R.id.btnCatalog)
        btnQuotations = findViewById(R.id.btnQuotations)
        btnMessaging = findViewById(R.id.btnMessaging)
        btnLogout = findViewById(R.id.btnLogout)

        // Initialize badge state
        updateNotificationBadge()
    }

    /**
     * Starts listening for new messages from admins in the user's chat thread.
     */
    private fun listenForAdminReplies(userEmail: String) {
        val chatId = encodeEmail(userEmail)
        chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId).child("messages")

        Log.d(TAG, "User Email: $userEmail")
        Log.d(TAG, "Chat ID: $chatId")
        Log.d(TAG, "Listening for admin replies at: chats/$chatId/messages")

        chatListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val msgKey = snapshot.key ?: return
                val msg = snapshot.getValue(ChatMessage::class.java) ?: return

                // Skip if already processed
                if (processedMessageKeys.contains(msgKey)) {
                    return
                }

                // Check if message is from an admin
                val isFromAdmin = isAdmin(msg.senderId)

                Log.d(TAG, "───────────────────────────────────")
                Log.d(TAG, "📨 NEW MESSAGE DETECTED")
                Log.d(TAG, "Message Key: $msgKey")
                Log.d(TAG, "Sender: ${msg.senderId}")
                Log.d(TAG, "Is From Admin: $isFromAdmin")
                Log.d(TAG, "Message: ${msg.message.take(50)}")
                Log.d(TAG, "───────────────────────────────────")

                // Process only admin messages (not user's own messages)
                if (isFromAdmin && msg.senderId != userEmail) {
                    processedMessageKeys.add(msgKey)
                    Log.i(TAG, "🔔 NEW ADMIN MESSAGE RECEIVED")
                    onNewAdminMessage(msg.message)
                } else {
                    Log.d(TAG, "⏭️ Skipping: User's own message or not from admin")
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
     * Called whenever an admin sends a new message.
     */
    private fun onNewAdminMessage(message: String) {
        Log.i(TAG, "Processing new admin message: ${message.take(40)}")
        updateNotificationBadgeStatic(true)
        showSystemNotification(message)
        showInAppNotification(message)
    }

    /**
     * Shows a subtle in-app toast notification
     */
    private fun showInAppNotification(message: String) {
        Toast.makeText(this, "💬 New message from Admin", Toast.LENGTH_SHORT).show()
    }

    /**
     * Updates the notification badge display
     */
    private fun updateNotificationBadge() {
        if (unreadMessageCount > 0) {
            notificationBadge.visibility = View.VISIBLE
            notificationBadge.text = if (unreadMessageCount > 99) "99+" else unreadMessageCount.toString()
            // Add pulse animation to badge
            ObjectAnimator.ofFloat(notificationBadge, "scaleX", 1f, 1.2f, 1f).apply {
                duration = 300
                start()
            }
            ObjectAnimator.ofFloat(notificationBadge, "scaleY", 1f, 1.2f, 1f).apply {
                duration = 300
                start()
            }
            Log.d(TAG, "📊 Badge updated: $unreadMessageCount unread message(s)")
        } else {
            notificationBadge.visibility = View.GONE
            Log.d(TAG, "📊 Badge hidden: no unread messages")
        }
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            else PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("FKJ Consultants - New Message")
            .setContentText(if (message.length > 40) message.take(40) + "..." else message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(this, R.color.primary))
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        Log.d(TAG, "📲 System notification displayed for new admin message")
    }

    /**
     * Creates a notification channel (Android O+).
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new chat messages from admins"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel '$CHANNEL_NAME' created")
        }
    }

    /**
     * Animates the bell icon and starts blinking RED when a new message arrives.
     */
    private fun pingAndBlinkBell() {
        Log.d(TAG, "🔴 Bell ping animation triggered - BLINKING RED")
        ObjectAnimator.ofFloat(notificationBell, "rotation", 0f, 25f, -25f, 0f).apply {
            duration = 400
            start()
        }
        startBellBlinking()
    }

    /**
     * Starts RED blinking effect on the bell icon for unread messages.
     */
    private fun startBellBlinking() {
        if (bellBlinking) return
        bellBlinking = true

        blinkRunnable = object : Runnable {
            override fun run() {
                if (notificationBell.alpha == 1f) {
                    // Fade out and turn red
                    notificationBell.alpha = 0.4f
                    notificationBell.setColorFilter(
                        ContextCompat.getColor(this@UserDashboardActivity, android.R.color.holo_red_dark)
                    )
                } else {
                    // Fade in and turn red
                    notificationBell.alpha = 1f
                    notificationBell.setColorFilter(
                        ContextCompat.getColor(this@UserDashboardActivity, android.R.color.holo_red_light)
                    )
                }
                if (bellBlinking) {
                    handler.postDelayed(this, 500)
                }
            }
        }
        handler.post(blinkRunnable!!)
        Log.d(TAG, "🔴 Bell blinking RED started")
    }

    /**
     * Stops the blinking effect on the bell icon and resets color.
     */
    private fun stopBellBlinking() {
        bellBlinking = false
        blinkRunnable?.let { handler.removeCallbacks(it) }
        notificationBell.alpha = 1f
        notificationBell.clearColorFilter() // Reset to original color
        Log.d(TAG, "Bell blinking stopped and color reset")
    }

    /**
     * Sets up the click listeners for buttons and icons on the dashboard.
     */
    private fun setupClickListeners() {
        btnCatalog.setOnClickListener {
            Log.d(TAG, "Navigating to CatalogActivity")
            startActivity(Intent(this, CatalogActivity::class.java))
        }

        btnQuotations.setOnClickListener {
            Log.d(TAG, "Navigating to QuotationActivity")
            startActivity(Intent(this, QuotationActivity::class.java))
        }

        btnMessaging.setOnClickListener {
            Log.d(TAG, "Navigating to ChatActivity via button")
            openChatActivity()
        }

        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        notificationBell.setOnClickListener {
            showNotificationPopup()
        }
    }

    /**
     * Shows logout confirmation dialog
     */
    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Performs the logout operation
     */
    private fun performLogout() {
        Log.i(TAG, "User logged out")
        auth.signOut()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    /**
     * Opens the chat activity and resets unread message indicators.
     */
    private fun openChatActivity() {
        Log.d(TAG, "Opening chat activity, resetting badge and bell")
        updateNotificationBadgeStatic(false)
        startActivity(Intent(this, ChatActivity::class.java))
    }

    /**
     * Displays a popup showing unread message count when the bell icon is tapped.
     */
    private fun showNotificationPopup() {
        val hasNew = unreadMessageCount > 0
        Log.d(TAG, "Notification popup opened. Has new: $hasNew, Count: $unreadMessageCount")

        AlertDialog.Builder(this)
            .setTitle(if (hasNew) "🔔 New Messages" else "💬 No New Messages")
            .setMessage(
                if (hasNew) "You have $unreadMessageCount unread message(s).\n\nGo to chat to view your messages?"
                else "No new messages.\n\nGo to chat to start a conversation with admin?"
            )
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
                R.id.nav_home -> {
                    // Already on home, just highlight
                    true
                }
                R.id.nav_catalog -> {
                    startActivity(Intent(this, CatalogActivity::class.java))
                    true
                }
                R.id.nav_dashboard -> {
                    // Already on dashboard
                    true
                }
                R.id.nav_messages -> {
                    openChatActivity()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Set home as selected by default
        bottomNavigationView.selectedItemId = R.id.nav_home
        Log.d(TAG, "Bottom navigation initialized")
    }

    override fun onResume() {
        super.onResume()
        // Clear processed messages when returning to dashboard
        // so we can detect new messages that arrived while away
        processedMessageKeys.clear()
        Log.d(TAG, "Activity resumed, cleared processed message cache")

        // Update badge in case it changed while activity was in background
        updateNotificationBadge()
    }

    override fun onPause() {
        super.onPause()
        // Stop blinking when activity goes to background to save battery
        stopBellBlinking()
    }

    /**
     * Cleans up Firebase listeners and stops blinking when activity is destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()
        chatListener?.let { chatRef?.removeEventListener(it) }
        stopBellBlinking()
        instance = null
        Log.i(TAG, "UserDashboardActivity destroyed and listeners removed")
    }
}