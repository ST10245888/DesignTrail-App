package vcmsa.projects.fkj_consultants.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class OrdersViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> get() = _orders

    fun loadOrders() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("orders")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _orders.value = snapshot.toObjects(Order::class.java)
                }
            }
    }

    fun placeOrder(itemName: String, quantity: Int) {
        val userId = auth.currentUser?.uid ?: return
        val orderId = db.collection("orders").document().id
        val order = Order(id = orderId, userId = userId, itemName = itemName, quantity = quantity)

        db.collection("orders").document(orderId).set(order)
    }
}

