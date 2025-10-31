package vcmsa.projects.fkj_consultants.activities

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
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

class AdminDashboardActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AdminDashboardActivity"
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var btnUserQuotations: Button
    private lateinit var btnInventory: Button
    private lateinit var btnMessaging: Button
    private lateinit var connectionStatusBanner: TextView
    private lateinit var connectedRef: DatabaseReference
    private lateinit var notificationBell: ImageView
    private lateinit var notificationBadge: TextView

    private var chatListenerReference: ChildEventListener? = null
    private val chatUnreadCounts = mutableMapOf<String, Int>()
    private var totalUnreadCount = 0
    private var isInitialLoad = true // Track if we're still loading existing chats
    private var initialLoadTimestamp = 0L

    private val handler = Handler(Looper.getMainLooper())
    private var bellBlinking = false
    private var blinkRunnable: Runnable? = null

    // Admin email list for checking
    private val adminEmails = listOf(
        "kush@gmail.com",
        "keitumetse01@gmail.com",
        "malikaOlivia@gmail.com",
        "JamesJameson@gmail.com"
    )

    // Aggregated notifications
    private val pendingNotifications = mutableMapOf<String, String>() // userEmail -> lastMessage
    private var notificationRunnable: Runnable? = null
    private val notificationDelay = 2000L // 2 seconds batching delay

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        auth = FirebaseAuth.getInstance()
        connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected")

        connectionStatusBanner = findViewById(R.id.connectionStatusBanner)
        btnUserQuotations = findViewById(R.id.btnUserQuotations)
        btnInventory = findViewById(R.id.btnInventory)
        btnMessaging = findViewById(R.id.btnMessaging)
        bottomNav = findViewById(R.id.adminBottomNav)
        notificationBell = findViewById(R.id.adminNotificationBell)
        notificationBadge = findViewById(R.id.adminNotificationBadge)

        setupButtonListeners()
        setupBottomNav()
        monitorFirebaseConnection()
        setupNotificationListener()

        Log.d(TAG, "Admin Dashboard initialized")
    }

    private fun setupButtonListeners() {
        btnUserQuotations.setOnClickListener { navigateTo(AdminQuotationListActivity::class.java) }
        btnInventory.setOnClickListener { navigateTo(InventoryActivity::class.java) }
        btnMessaging.setOnClickListener { openMessagingDashboard() }

        notificationBell.setOnClickListener {
            showNotificationPopup()
            stopBellBlinking()
        }
    }

    private fun setupBottomNav() {
        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_quotations -> { navigateTo(AdminQuotationListActivity::class.java); true }
                R.id.nav_inventory -> { navigateTo(InventoryActivity::class.java); true }
                R.id.nav_messages -> { openMessagingDashboard(); true }
                else -> false
            }
        }
    }

    private fun openMessagingDashboard() {
        if (auth.currentUser == null) {
            Toast.makeText(this, "Admin not logged in", Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(Intent(this, AdminChatDashboardActivity::class.java))
        stopBellBlinking()
        pendingNotifications.clear() // Clear notifications when opening chat
    }

    private fun navigateTo(targetActivity: Class<*>) {
        startActivity(Intent(this, targetActivity))
    }

    private fun monitorFirebaseConnection() {
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                showConnectionStatus(connected)
            }

            override fun onCancelled(error: DatabaseError) {
                showConnectionStatus(false)
            }
        })
    }

    private fun showConnectionStatus(isConnected: Boolean) {
        connectionStatusBanner.visibility = View.VISIBLE
        if (isConnected) {
            connectionStatusBanner.text = "Online"
            connectionStatusBanner.setBackgroundColor(getColor(R.color.status_online))
        } else {
            connectionStatusBanner.text = "Offline - Using Cached Data"
            connectionStatusBanner.setBackgroundColor(getColor(R.color.status_offline))
        }
    }

    private fun setupNotificationListener() {
        val chatsRef = FirebaseDatabase.getInstance().getReference("chats")

        chatListenerReference = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) = handleChatUpdate(snapshot)
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) = handleChatUpdate(snapshot)
            override fun onChildRemoved(snapshot: DataSnapshot) {
                val chatId = snapshot.key ?: return
                chatUnreadCounts.remove(chatId)
                pendingNotifications.remove(snapshot.child("metadata").child("userEmail").getValue(String::class.java))
                updateTotalUnreadCount()
                scheduleAggregatedNotification()
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Notification listener cancelled: ${error.message}")
            }
        }

        chatsRef.addChildEventListener(chatListenerReference!!)
    }

    private fun handleChatUpdate(snapshot: DataSnapshot) {
        val chatId = snapshot.key ?: return
        val metadata = snapshot.child("metadata")

        // Get current admin's email
        val currentAdminEmail = auth.currentUser?.email ?: return
        val encodedAdminEmail = encodeEmail(currentAdminEmail)

        // Get unread count for THIS admin specifically from unreadCounts map
        val unreadCount = metadata.child("unreadCounts")
            .child(encodedAdminEmail)
            .getValue(Int::class.java) ?: 0

        val userEmail = metadata.child("userEmail").getValue(String::class.java) ?: "User"
        val lastMessage = metadata.child("lastMessage").getValue(String::class.java) ?: ""
        val lastSenderId = metadata.child("lastSenderId").getValue(String::class.java) ?: ""

        // Check if message is from a user (not from any admin)
        val isFromUser = !isAdminEmail(lastSenderId)

        Log.d(TAG, "═══════════════════════════════════")
        Log.d(TAG, "📬 CHAT UPDATE RECEIVED")
        Log.d(TAG, "ChatId: $chatId")
        Log.d(TAG, "User Email: $userEmail")
        Log.d(TAG, "Current Admin: $currentAdminEmail")
        Log.d(TAG, "Encoded Admin: $encodedAdminEmail")
        Log.d(TAG, "Last Sender: $lastSenderId")
        Log.d(TAG, "Is From User: $isFromUser")
        Log.d(TAG, "Unread Count: $unreadCount")
        Log.d(TAG, "Last Message: $lastMessage")
        Log.d(TAG, "═══════════════════════════════════")

        if (unreadCount > 0 && isFromUser) {
            val previousCount = chatUnreadCounts[chatId] ?: 0

            Log.d(TAG, "✅ CONDITION MET: unreadCount=$unreadCount > 0 AND isFromUser=$isFromUser")
            Log.d(TAG, "Previous count: $previousCount, New count: $unreadCount")

            // Only trigger notification if count actually increased
            if (previousCount != unreadCount) {
                chatUnreadCounts[chatId] = unreadCount
                updateTotalUnreadCount()
                pingAndBlinkBell()

                // Add/update pending notification
                pendingNotifications[userEmail] = lastMessage
                scheduleAggregatedNotification()

                Log.d(TAG, "🔔 NOTIFICATION TRIGGERED from $userEmail: $lastMessage (count: $previousCount -> $unreadCount)")
            } else {
                Log.d(TAG, "⚠️ Count unchanged, skipping notification")
            }
        } else {
            Log.d(TAG, "❌ CONDITION NOT MET:")
            Log.d(TAG, "   - unreadCount > 0: ${unreadCount > 0}")
            Log.d(TAG, "   - isFromUser: $isFromUser")

            // No unread messages or message from admin - clear this chat
            if (chatUnreadCounts.containsKey(chatId)) {
                chatUnreadCounts.remove(chatId)
                pendingNotifications.remove(userEmail)
                updateTotalUnreadCount()
                Log.d(TAG, "✅ Cleared notifications for $userEmail")
            }
        }
    }

    private fun encodeEmail(email: String) = email.replace(".", ",")

    private fun isAdminEmail(email: String): Boolean {
        return adminEmails.any { it.equals(email, ignoreCase = true) }
    }

    private fun updateTotalUnreadCount() {
        totalUnreadCount = chatUnreadCounts.values.sum()
        runOnUiThread {
            if (totalUnreadCount > 0) {
                notificationBadge.visibility = View.VISIBLE
                notificationBadge.text = if (totalUnreadCount > 99) "99+" else totalUnreadCount.toString()
                Log.d(TAG, "📊 Total unread: $totalUnreadCount")
            } else {
                notificationBadge.visibility = View.GONE
                Log.d(TAG, "📊 No unread messages")
            }
        }
    }

    private fun pingAndBlinkBell() {
        runOnUiThread {
            Log.d(TAG, "🔴 Admin bell ping animation triggered - BLINKING RED")
            ObjectAnimator.ofFloat(notificationBell, "rotation", 0f, 25f, -25f, 0f).apply {
                duration = 300
                start()
            }
            startBellBlinking()
        }
    }

    private fun startBellBlinking() {
        if (bellBlinking) return
        bellBlinking = true
        blinkRunnable = object : Runnable {
            override fun run() {
                if (notificationBell.alpha == 1f) {
                    // Fade out and turn red
                    notificationBell.alpha = 0.4f
                    notificationBell.setColorFilter(
                        getColor(android.R.color.holo_red_dark)
                    )
                } else {
                    // Fade in and turn red
                    notificationBell.alpha = 1f
                    notificationBell.setColorFilter(
                        getColor(android.R.color.holo_red_light)
                    )
                }
                handler.postDelayed(this, 500)
            }
        }
        handler.post(blinkRunnable!!)
        Log.d(TAG, "🔴 Admin bell blinking RED started")
    }

    private fun stopBellBlinking() {
        bellBlinking = false
        blinkRunnable?.let { handler.removeCallbacks(it) }
        notificationBell.alpha = 1f
        notificationBell.clearColorFilter() // Reset to original color
        Log.d(TAG, "Admin bell blinking stopped and color reset")
    }

    // Schedule the aggregated notification with batching delay
    private fun scheduleAggregatedNotification() {
        notificationRunnable?.let { handler.removeCallbacks(it) }
        notificationRunnable = Runnable { sendAggregatedAdminNotification() }
        handler.postDelayed(notificationRunnable!!, notificationDelay)
    }

    private fun sendAggregatedAdminNotification() {
        if (pendingNotifications.isEmpty()) return

        val channelId = "admin_messages_channel"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Admin Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Notifications for new user messages" }
            notificationManager.createNotificationChannel(channel)
        }

        val title = if (pendingNotifications.size == 1) {
            "New message from ${pendingNotifications.keys.first()}"
        } else {
            "${pendingNotifications.size} users sent new messages"
        }

        val messageText = if (pendingNotifications.size == 1) {
            pendingNotifications.values.first()
        } else {
            pendingNotifications.entries.joinToString("\n") { (user, msg) ->
                "$user: ${if (msg.length > 40) msg.take(40) + "…" else msg}"
            }
        }

        val intent = Intent(this, AdminChatDashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            else PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText("Tap to view messages")
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageText))
            .setSmallIcon(R.drawable.ic_notification_bell)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(1001, notification) 

        Log.d(TAG, "📲 Push notification sent: $title")
    }

    private fun showNotificationPopup() {
        val title = if (totalUnreadCount > 0) "Unread Messages" else "No New Messages"
        val message = if (totalUnreadCount > 0)
            "You have $totalUnreadCount unread message${if (totalUnreadCount > 1) "s" else ""}. Open chat?"
        else "No new messages at the moment."

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Open Chat") { _, _ -> openMessagingDashboard() }
            .setNegativeButton("Dismiss", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        chatListenerReference?.let {
            FirebaseDatabase.getInstance().getReference("chats").removeEventListener(it)
        }
        stopBellBlinking()
        notificationRunnable?.let { handler.removeCallbacks(it) }
    }
}