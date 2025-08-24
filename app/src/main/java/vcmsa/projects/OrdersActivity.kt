package vcmsa.projects.FKJ_Consultants

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import vcmsa.projects.fkj_consultants.R

class OrdersActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        // Header title
        val tvOrdersTitle: TextView = findViewById(R.id.tvOrdersTitle)

        // Order items and their hidden details
        val orderItem1: LinearLayout = findViewById(R.id.orderItem1)
        val details1: LinearLayout = findViewById(R.id.details1)

        val orderItem2: LinearLayout = findViewById(R.id.orderItem2)
        val details2: LinearLayout = findViewById(R.id.details2)

        val orderItem3: LinearLayout = findViewById(R.id.orderItem3)
        val details3: LinearLayout = findViewById(R.id.details3)

        // Toggle visibility on clicking order items
        orderItem1.setOnClickListener { toggleVisibility(details1) }
        orderItem2.setOnClickListener { toggleVisibility(details2) }
        orderItem3.setOnClickListener { toggleVisibility(details3) }


    }

    private fun toggleVisibility(view: View) {
        view.visibility = if (view.visibility == View.GONE) View.VISIBLE else View.GONE
    }
}
