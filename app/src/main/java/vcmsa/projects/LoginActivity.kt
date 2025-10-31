package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import vcmsa.projects.fkj_consultants.R

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var tvForgotPassword: TextView
    private lateinit var ivShowPassword: ImageView
    private var isPasswordVisible = false

    companion object {
        private const val TAG = "LoginActivity"

        // Extended trusted admin list
        val ADMIN_EMAILS = listOf(
            "kush@gmail.com",
            "keitumetse01@gmail.com",
            "malikaOlivia@gmail.com",
            "JamesJameson@gmail.com"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initializeFirebase()
        initializeViews()
        setupPasswordToggle()
        setupClickListeners()
    }

    /** ---------------- Firebase Initialization ---------------- */
    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance()
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (e: Exception) {
            Log.w(TAG, "Firebase persistence already enabled", e)
        }
        dbRef = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.keepSynced(true)
    }

    /** ---------------- View Initialization ---------------- */
    private fun initializeViews() {
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        ivShowPassword = findViewById(R.id.ivShowPassword)
    }

    /** ---------------- Password Visibility Toggle ---------------- */
    private fun setupPasswordToggle() {
        ivShowPassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivShowPassword.setImageResource(R.drawable.ic_eye_on)
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivShowPassword.setImageResource(R.drawable.ic_eye_off)
            }
            etPassword.setSelection(etPassword.text.length)
        }
    }

    /** ---------------- Click Listeners ---------------- */
    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            if (validateInputs()) loginUser()
        }

        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    /** ---------------- Input Validation ---------------- */
    private fun validateInputs(): Boolean {
        val email = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()

        return when {
            email.isEmpty() -> {
                etUsername.error = "Email is required"
                etUsername.requestFocus()
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                etUsername.error = "Enter a valid email"
                etUsername.requestFocus()
                false
            }
            password.isEmpty() -> {
                etPassword.error = "Password is required"
                etPassword.requestFocus()
                false
            }
            password.length < 6 -> {
                etPassword.error = "Password must be at least 6 characters"
                etPassword.requestFocus()
                false
            }
            else -> true
        }
    }

    /** ---------------- Login Process ---------------- */
    private fun loginUser() {
        val email = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()

        btnLogin.isEnabled = false
        btnLogin.text = "Logging in..."

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val currentUser = authResult.user
                if (currentUser != null) {
                    Log.d(TAG, "Login successful: ${currentUser.email}")
                    updateLastLogin(currentUser.uid)
                    routeUser(currentUser.email)
                } else {
                    restoreLoginButton()
                    Toast.makeText(this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                restoreLoginButton()
                Log.e(TAG, "Login failed", exception)
                handleLoginError(exception.message)
            }
    }

    private fun restoreLoginButton() {
        btnLogin.isEnabled = true
        btnLogin.text = "Login"
    }

    /** ---------------- Error Handling ---------------- */
    private fun handleLoginError(message: String?) {
        val errorMessage = when {
            message?.contains("no user record", ignoreCase = true) == true -> "No account found with this email"
            message?.contains("password is invalid", ignoreCase = true) == true -> "Incorrect password"
            message?.contains("network error", ignoreCase = true) == true -> "Network error. Check your connection"
            else -> "Login failed: ${message ?: "Unknown error"}"
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    /** ---------------- Update Last Login ---------------- */
    private fun updateLastLogin(uid: String) {
        val updates = mapOf("lastLogin" to System.currentTimeMillis())
        dbRef.child(uid).updateChildren(updates)
            .addOnSuccessListener { Log.d(TAG, "Last login updated successfully") }
            .addOnFailureListener { Log.w(TAG, "Failed to update last login", it) }
    }

    /** ---------------- Route User Based on Role ---------------- */
    private fun routeUser(email: String?) {
        if (email == null) {
            Toast.makeText(this, "Login error. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }

        val isAdmin = ADMIN_EMAILS.any { it.equals(email, ignoreCase = true) }
        val destination = if (isAdmin)
            AdminDashboardActivity::class.java
        else
            UserDashboardActivity::class.java

        val msg = if (isAdmin) "Admin Login Successful" else "Login Successful"
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

        startActivity(Intent(this, destination))
        finishAffinity()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}



/*


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
        "keitumetse01@gmail.com"
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


*/
