package vcmsa.projects.fkj_consultants.utils

import android.util.Log
import com.google.firebase.database.*

object DatabaseCleanupUtil {
    private const val TAG = "DatabaseCleanup"

    fun cleanupInvalidData(callback: (success: Boolean, message: String) -> Unit) {
        val dbRef = FirebaseDatabase.getInstance().getReference("inventory")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var fixedCount = 0
                var errorCount = 0

                for (item in snapshot.children) {
                    try {
                        val updates = mutableMapOf<String, Any>()

                        // Fix price if it's a string
                        val priceValue = item.child("price").value
                        if (priceValue is String) {
                            val doublePrice = priceValue.toDoubleOrNull() ?: 0.0
                            updates["price"] = doublePrice
                            fixedCount++
                        }

                        // Fix quantity if it's a string
                        val qtyValue = item.child("quantity").value
                        if (qtyValue is String) {
                            val intQty = qtyValue.toIntOrNull() ?: 0
                            updates["quantity"] = intQty
                            fixedCount++
                        }

                        // Fix timestamp if it's a string
                        val tsValue = item.child("timestamp").value
                        if (tsValue is String) {
                            val longTs = tsValue.toLongOrNull() ?: System.currentTimeMillis()
                            updates["timestamp"] = longTs
                            fixedCount++
                        }

                        // Apply updates if any
                        if (updates.isNotEmpty()) {
                            item.ref.updateChildren(updates)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fixing item ${item.key}", e)
                        errorCount++
                    }
                }

                val message = "Fixed $fixedCount fields. Errors: $errorCount"
                Log.i(TAG, message)
                callback(errorCount == 0, message)
            }

            override fun onCancelled(error: DatabaseError) {
                val message = "Cleanup failed: ${error.message}"
                Log.e(TAG, message, error.toException())
                callback(false, message)
            }
        })
    }
}