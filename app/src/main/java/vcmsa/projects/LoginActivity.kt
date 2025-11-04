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

    // Firebase authentication and database references (Android Developers, 2024)
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    // UI components
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var tvForgotPassword: TextView
    private lateinit var ivShowPassword: ImageView

    // State variable for password visibility toggle
    private var isPasswordVisible = false

    companion object {
        private const val TAG = "LoginActivity"

        // Trusted admin email list for privileged access (Android Developers, 2024)
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

        // Initialize Firebase, views, and event listeners (Android Developers, 2024)
        initializeFirebase()
        initializeViews()
        setupPasswordToggle()
        setupClickListeners()
    }

    /** ---------------- Firebase Initialization ---------------- */
    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance() // Get Firebase authentication instance
        try {
            // Enable offline persistence for real-time database
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (e: Exception) {
            Log.w(TAG, "Firebase persistence already enabled", e)
        }
        // Reference to "Users" node in Firebase Database
        dbRef = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.keepSynced(true) // Keep user data synchronized
    }

    /** ---------------- View Initialization ---------------- */
    private fun initializeViews() { // (GeeksforGeeks, 2024)
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        ivShowPassword = findViewById(R.id.ivShowPassword)
    }

    /** ---------------- Password Visibility Toggle ---------------- */
    private fun setupPasswordToggle() { // (GeeksforGeeks, 2024)
        ivShowPassword.setOnClickListener {
            // Toggle password visibility state
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                // Show password
                etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivShowPassword.setImageResource(R.drawable.ic_eye_on)
            } else {
                // Hide password (GeeksforGeeks, 2024)
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivShowPassword.setImageResource(R.drawable.ic_eye_off)
            }
            etPassword.setSelection(etPassword.text.length) // Keep cursor at end
        }
    }

    /** ---------------- Click Listeners ---------------- */
    private fun setupClickListeners() { // (Kotlinlang.org, 2024)
        // Login button listener
        btnLogin.setOnClickListener {
            if (validateInputs()) loginUser()
        }

        // Redirect to Forgot Password page
        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // Redirect to Registration page
        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    /** ---------------- Input Validation ---------------- */
    private fun validateInputs(): Boolean {
        val email = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Stepwise validation for email and password (GeeksforGeeks, 2024)
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

        // Disable login button during process (Android Developers, 2024)
        btnLogin.isEnabled = false
        btnLogin.text = "Logging in..."

        // Firebase Authentication using email and password (Android Developers, 2024)
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val currentUser = authResult.user
                if (currentUser != null) {
                    Log.d(TAG, "Login successful: ${currentUser.email}")
                    updateLastLogin(currentUser.uid)
                    routeUser(currentUser.email) // Redirect user by role (Kotlinlang.org, 2024)
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

    // Restores login button UI after failure (Kotlinlang.org, 2024)
    private fun restoreLoginButton() {
        btnLogin.isEnabled = true
        btnLogin.text = "Login"
    }

    /** ---------------- Error Handling (Google, 2024) */
    private fun handleLoginError(message: String?) {
        // Map Firebase errors to user-friendly messages (Google, 2024)
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
        // Updates timestamp of last login for analytics and audit (Kotlinlang.org, 2024)
        val updates = mapOf("lastLogin" to System.currentTimeMillis())
        dbRef.child(uid).updateChildren(updates)
            .addOnSuccessListener { Log.d(TAG, "Last login updated successfully") }
            .addOnFailureListener { Log.w(TAG, "Failed to update last login", it) }
    }

    /** ---------------- Route User Based on Role (Android Developers, 2024) */ 
    private fun routeUser(email: String?) {
        if (email == null) {
            Toast.makeText(this, "Login error. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if user is admin or regular user (Kotlinlang.org, 2024)
        val isAdmin = ADMIN_EMAILS.any { it.equals(email, ignoreCase = true) }
        val destination = if (isAdmin)
            AdminDashboardActivity::class.java
        else
            UserDashboardActivity::class.java

        val msg = if (isAdmin) "Admin Login Successful" else "Login Successful"
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

        // Navigate to the respective dashboard (Kotlinlang.org, 2024)
        startActivity(Intent(this, destination))
        finishAffinity() // Clears activity stack
    }

    // Override back button to close all previous activities (Kotlinlang.org, 2024)
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}

/*
Reference List

Android Developers, 2024. Firebase Authentication Overview. [online] Available at: https://firebase.google.com/docs/auth/android/start [Accessed 14 August 2025].

GeeksforGeeks, 2024. Implementing Login and Registration using Firebase in Android. [online] Available at: https://www.geeksforgeeks.org/login-and-registration-using-firebase-in-android/ [Accessed 14 August 2025].
 
Google, 2024. Firebase Error Handling and Exceptions. [online] Available at: https://firebase.google.com/docs/reference/android/com/google/firebase/FirebaseException [Accessed 15 August 2025].

Kotlinlang.org, 2024. Kotlin Android Development Best Practices. [online] Available at: https://kotlinlang.org/docs/android-overview.html [Accessed 14 August 2025].

 */
