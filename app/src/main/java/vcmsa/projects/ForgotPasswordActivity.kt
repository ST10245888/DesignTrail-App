package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import vcmsa.projects.fkj_consultants.R

class ForgotPasswordActivity : AppCompatActivity() {

    // Firebase authentication instance (Android Developers, 2024)
    private lateinit var auth: FirebaseAuth

    // Tag used for logging errors in Logcat
    private val TAG = "ForgotPassword"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Initialize Firebase Auth (Android Developers, 2024)
        auth = FirebaseAuth.getInstance()

        // Link XML views to Kotlin variables (Kotlinlang.org, 2024)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val btnResetPassword = findViewById<Button>(R.id.btnResetPassword)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)

        // Handle reset password button click (GeeksforGeeks, 2024)
        btnResetPassword.setOnClickListener {
            val email = etEmail.text.toString().trim() // Get email input and remove spaces

            // Validate that the email field is not empty (Kotlinlang.org, 2024)
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Stop execution if no email is provided
            }

            // Firebase sends a password reset email to the provided address (GeeksforGeeks, 2024)
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Notify the user that the email was successfully sent (Kotlinlang.org, 2024)
                        Toast.makeText(
                            this,
                            "Password reset email sent to: $email\nCheck your inbox or spam folder.",
                            Toast.LENGTH_LONG
                        ).show()

                        // Navigate back to the login screen (Kotlinlang.org, 2024)
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish() // Close this activity
                    } else {
                        // Handle various failure cases and provide meaningful messages
                        val exception = task.exception
                        val errorMessage = when (exception) {
                            is FirebaseAuthInvalidUserException ->
                                "No account found with this email address. Please check and try again."

                            is FirebaseAuthInvalidCredentialsException ->
                                "Invalid email format. Please enter a valid email address."

                            is FirebaseNetworkException ->
                                "Network error. Please check your internet connection and try again."

                            else ->
                                "Password reset failed. ${exception?.localizedMessage ?: "Unknown error occurred."}"
                        }

                        // Log detailed error for debugging
                        Log.e(TAG, "Password reset failed: ", exception)

                        // Display the error message to the user
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
        }

        // Navigate back to login screen when the user clicks "Back to Login" (Kotlinlang.org, 2024)
        tvBackToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}

/*
Reference List

Android Developers, 2024. Firebase Authentication: Send Password Reset Emails. [online] Available at: https://firebase.google.com/docs/auth/android/manage-users#send_a_password_reset_email [Accessed 12 October 2025].

GeeksforGeeks, 2024. Implementing Forgot Password Feature in Android using Firebase. [online] Available at: https://www.geeksforgeeks.org/implement-forgot-password-feature-in-android-using-firebase/ [Accessed 12 October 2025].

Kotlinlang.org, 2024. Android App Development with Kotlin. [online] Available at: https://kotlinlang.org/docs/android-overview.html [Accessed 12 October 2025].

 */
