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

    // Firebase authentication instance
    private lateinit var auth: FirebaseAuth

    // Tag used for logging errors in Logcat
    private val TAG = "ForgotPassword"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Link XML views to Kotlin variables
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val btnResetPassword = findViewById<Button>(R.id.btnResetPassword)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)

        // Handle reset password button click
        btnResetPassword.setOnClickListener {
            val email = etEmail.text.toString().trim() // Get email input and remove spaces

            // Validate that the email field is not empty
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Stop execution if no email is provided
            }

            // Firebase sends a password reset email to the provided address
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Notify the user that the email was successfully sent
                        Toast.makeText(
                            this,
                            "Password reset email sent to: $email\nCheck your inbox or spam folder.",
                            Toast.LENGTH_LONG
                        ).show()

                        // Navigate back to the login screen
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

        // Navigate back to login screen when the user clicks "Back to Login"
        tvBackToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}