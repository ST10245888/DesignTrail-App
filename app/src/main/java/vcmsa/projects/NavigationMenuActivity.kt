package vcmsa.projects.fkj_consultants

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class NavigationMenuActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation_menu)

        // Initialize BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        // Set navigation item click listener
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    onNavigationItemSelected("home")
                    true
                }
                R.id.nav_catalog -> {
                    onNavigationItemSelected("catalog")
                    true
                }
                R.id.nav_dashboard -> {
                    onNavigationItemSelected("dashboard")
                    true
                }
                R.id.nav_messages -> {
                    onNavigationItemSelected("messages")
                    true
                }
                R.id.nav_profile -> {
                    onNavigationItemSelected("profile")
                    true
                }
                else -> false
            }
        }
    }

    private fun onNavigationItemSelected(destination: String) {
        // Override this method in child activities to handle navigation
        // Example: Load fragments or start new activities
    }
}