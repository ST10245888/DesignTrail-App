/*package vcmsa.projects.fkj_consultants.activities*/

package student.projects.fkj_consultants_app.activities

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import student.projects.fkj_consultants_app.R

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    // Trusted admin emails (case-insensitive)
    private val adminEmails = listOf(
        "kush@gmail.com",
        "keitumetse01@gmail.com",
        "malikaOlivia@gmail.com",
        "JamesJameson@gmail.com"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

        // Login button click
        btnLogin.setOnClickListener {
            val email = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validate inputs
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Sign out any cached user
            auth.signOut()

            // Firebase login
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    val currentUser = auth.currentUser
                    if (task.isSuccessful && currentUser != null) {
                        // Case-insensitive check for admin emails
                        if (adminEmails.any { it.equals(currentUser.email, ignoreCase = true) }) {
                            Toast.makeText(this, "Admin Login Successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, AdminDashboardActivity::class.java))
                        } else {
                            Toast.makeText(this, "User Login Successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, UserDashboardActivity::class.java))
                        }
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "Invalid email or password: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        // Forgot Password click
        tvForgotPassword.setOnClickListener {
            // Navigate to ForgotPasswordActivity
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }
}
