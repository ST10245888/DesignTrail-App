package vcmsa.projects.fkj_consultants

import android.app.Application
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        initializeAppCheck()
        configureRealtimeDatabase()
        configureFirestore()
        preloadEssentialData()
        testDataSynchronization()
    }

    /** App Check configuration */
    private fun initializeAppCheck() {
        val appCheck = FirebaseAppCheck.getInstance()
        if (BuildConfig.DEBUG) {
            appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
            Log.d("MyApplication", "AppCheck: Debug factory active.")
        } else {
            appCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance())
            Log.d("MyApplication", "AppCheck: Play Integrity active.")
        }
    }

    /** Ensure persistence enabled only once, before DB usage */
    private fun configureRealtimeDatabase() {
        val db = FirebaseDatabase.getInstance()
        try {
            db.setPersistenceEnabled(true)
            Log.d("MyApplication", "Realtime DB persistence enabled.")
        } catch (e: Exception) {
            Log.w("MyApplication", "Persistence already enabled, continuing.")
        }

        // Keep important nodes synced
        db.getReference("inventory").keepSynced(true)
        db.getReference("config").keepSynced(true)
    }

    private fun configureFirestore() {
        val firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        firestore.firestoreSettings = settings
        Log.d("MyApplication", "Firestore cache configured.")
    }

    private fun preloadEssentialData() {
        val firestore = FirebaseFirestore.getInstance()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Tasks.await(firestore.collection("config").get(Source.DEFAULT))
                Tasks.await(firestore.collection("categories").get(Source.DEFAULT))
                Tasks.await(firestore.collection("promotions").get(Source.DEFAULT))
                Log.d("MyApplication", "Preload complete.")
            } catch (e: Exception) {
                Log.w("MyApplication", "Preload failed: ${e.message}")
            }
        }
    }

    private fun testDataSynchronization() {
        val dbRef = FirebaseDatabase.getInstance().reference.child("syncTest")
        val testData = mapOf("timestamp" to System.currentTimeMillis(), "status" to "active")

        dbRef.setValue(testData)
            .addOnSuccessListener { Log.d("MyApplication", "RTDB sync test queued.") }
            .addOnFailureListener { Log.w("MyApplication", "Sync test failed: ${it.message}") }

        val firestore = FirebaseFirestore.getInstance()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val local = Tasks.await(firestore.collection("config").get(Source.CACHE))
                val remote = Tasks.await(firestore.collection("config").get(Source.SERVER))
                Log.d("MyApplication", "Firestore sync check: local=${local.size()} server=${remote.size()}")
            } catch (e: Exception) {
                Log.w("MyApplication", "Sync verify failed: ${e.message}")
            }
        }
    }
}
