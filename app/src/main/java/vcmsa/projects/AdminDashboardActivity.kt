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

    private val handler = Handler(Looper.getMainLooper())
    private var bellBlinking = false
    private var blinkRunnable: Runnable? = null

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
        val unreadCount = metadata.child("unreadCount").getValue(Int::class.java) ?: 0
        val userEmail = metadata.child("userEmail").getValue(String::class.java) ?: "User"
        val lastMessage = metadata.child("lastMessage").getValue(String::class.java) ?: ""
        val lastSenderId = metadata.child("lastSenderId").getValue(String::class.java) ?: ""

        if (unreadCount > 0 && lastSenderId != "admin") {
            if (chatUnreadCounts[chatId] != unreadCount) {
                chatUnreadCounts[chatId] = unreadCount
                updateTotalUnreadCount()
                pingAndBlinkBell()

                // Add/update pending notification
                pendingNotifications[userEmail] = lastMessage
                scheduleAggregatedNotification()

                Log.d(TAG, "🔔 New message from $userEmail: $lastMessage")
            }
        } else {
            chatUnreadCounts.remove(chatId)
            pendingNotifications.remove(userEmail)
            updateTotalUnreadCount()
            scheduleAggregatedNotification()
        }
    }

    private fun updateTotalUnreadCount() {
        totalUnreadCount = chatUnreadCounts.values.sum()
        runOnUiThread {
            if (totalUnreadCount > 0) {
                notificationBadge.visibility = View.VISIBLE
                notificationBadge.text = if (totalUnreadCount > 99) "99+" else totalUnreadCount.toString()
            } else {
                notificationBadge.visibility = View.GONE
            }
        }
    }

    private fun pingAndBlinkBell() {
        runOnUiThread {
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
                notificationBell.alpha = if (notificationBell.alpha == 1f) 0.4f else 1f
                handler.postDelayed(this, 500)
            }
        }
        handler.post(blinkRunnable!!)
    }

    private fun stopBellBlinking() {
        bellBlinking = false
        blinkRunnable?.let { handler.removeCallbacks(it) }
        notificationBell.alpha = 1f
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

        notificationManager.notify(1001, notification) // fixed ID to update notification
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
