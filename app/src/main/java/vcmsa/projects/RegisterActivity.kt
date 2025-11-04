package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.R.id

class RegisterActivity : AppCompatActivity() {

    // Firebase Authentication instance used to handle account creation (Android Developers, 2024)
    private lateinit var auth: FirebaseAuth

    // Declaring EditText and Button views for user input and actions
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView

    companion object { // Static-like container for constants (Kotlinlang.org, 2024)
        private const val TAG = "RegisterActivity" // Used for Logcat debugging
        private val PHONE_PATTERN = Regex("^\\+27\\d{9}\$") // South African phone number pattern
    }

    override fun onCreate(savedInstanceState: Bundle?) { // Activity lifecycle method (Kotlinlang.org, 2024)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register) // Set XML layout for registration page

        // Initialize Firebase authentication object (Android Developers, 2024)
        auth = FirebaseAuth.getInstance()

        // Initialize all UI components
        initializeViews()
        // Setup auto-formatting for South African phone numbers
        setupPhoneFormatting()
        // Setup click listeners for buttons and navigation links
        setupClickListeners()
    }

    // Function to connect XML elements with Kotlin variables
    private fun initializeViews() {
        etFirstName = findViewById(id.etFirstName)
        etLastName = findViewById(id.etLastName)
        etEmail = findViewById(id.etEmail)
        etPhone = findViewById(id.etPhone)
        etPassword = findViewById(id.etPassword)
        etConfirmPassword = findViewById(id.etConfirmPassword)
        btnRegister = findViewById(id.btnRegister)
        tvLogin = findViewById(id.tvLoginRedirect) // Initialize TextView for redirect to login screen
    }

    // Ensures the phone number always starts with "+27" and has a max length of 12 (GeeksforGeeks, 2024)
    private fun setupPhoneFormatting() {
        etPhone.setText("+27") // Default prefix for South African numbers
        etPhone.setSelection(etPhone.text.length)
        etPhone.filters = arrayOf(InputFilter.LengthFilter(12)) // Restrict input length

        // Add a TextWatcher to enforce "+27" prefix dynamically
        etPhone.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false // Prevent recursive updates

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return // Avoid infinite loop
                isFormatting = true
                // If user removes or changes "+27", reset it
                if (s != null && !s.startsWith("+27")) {
                    etPhone.setText("+27")
                    etPhone.setSelection(etPhone.text.length)
                }
                isFormatting = false
            }
        })
    }

    // Handles all click events (Android Developers, 2024)
    private fun setupClickListeners() {
        // When user clicks "Register"
        btnRegister.setOnClickListener {
            if (validateInputs()) { // Validate all input fields before registration
                registerUser()
            }
        }

        // Navigate to LoginActivity when "Already have an account?" is clicked
        tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Close registration activity
        }
    }

    // Validate user inputs such as email, password, and phone number (Kotlinlang.org, 2024)
    private fun validateInputs(): Boolean {
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        // Sequential input checks (Google, 2024)
        when {
            firstName.isEmpty() -> {
                etFirstName.error = "First name is required"; return false
            }
            lastName.isEmpty() -> {
                etLastName.error = "Last name is required"; return false
            }
            email.isEmpty() -> {
                etEmail.error = "Email is required"; return false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                etEmail.error = "Enter a valid email"; return false
            }
            phone.isEmpty() -> {
                etPhone.error = "Phone number is required"; return false
            }
            !PHONE_PATTERN.matches(phone) -> {
                etPhone.error = "Enter a valid phone number (e.g. +27821234567)"; return false
            }
            password.isEmpty() -> {
                etPassword.error = "Password is required"; return false
            }
            password.length < 8 -> {
                etPassword.error = "Password must be at least 8 characters"; return false
            }
            !password.matches(Regex(".*[A-Z].*")) -> {
                etPassword.error = "Password must contain at least one uppercase letter"; return false
            }
            !password.matches(Regex(".*[a-z].*")) -> {
                etPassword.error = "Password must contain at least one lowercase letter"; return false
            }
            !password.matches(Regex(".*\\d.*")) -> {
                etPassword.error = "Password must contain at least one number"; return false
            }
            password != confirmPassword -> {
                etConfirmPassword.error = "Passwords do not match"; return false
            }
        }
        return true // All inputs are valid
    }

    // Creates a new Firebase user account (Kotlinlang.org, 2024)
    private fun registerUser() {
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString()

        // Disable the button while registration is in progress
        btnRegister.isEnabled = false
        btnRegister.text = "Registering..." // Feedback to user (Android Developers, 2024)

        // Create a new user account in Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser // Get newly created Firebase user
                    user?.let {
                        // Save user details into the Realtime Database
                        saveUserToDatabase(it.uid, firstName, lastName, email, phone)
                    }
                } else {
                    // Re-enable button if registration fails
                    btnRegister.isEnabled = true
                    btnRegister.text = "Register"
                    Log.e(TAG, "Registration failed", task.exception) // Log error for debugging

                    // Show error message on screen (Google, 2024)
                    Toast.makeText(
                        this,
                        "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    // Save user profile information to Firebase Realtime Database (Android Developers, 2024)
    private fun saveUserToDatabase(
        uid: String,
        firstName: String,
        lastName: String,
        email: String,
        phone: String
    ) { // (GeeksforGeeks, 2024)
        val currentTime = System.currentTimeMillis() // Track account creation time

        // Map to store user data in key-value pairs
        val userMap = mapOf(
            "uid" to uid,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "phoneNumber" to phone,
            "role" to "user", // Default role assigned on registration
            "createdAt" to currentTime,
            "lastLogin" to currentTime
        )

        // Save the user record to Firebase under "Users" node (Android Developers, 2024)
        FirebaseDatabase.getInstance().getReference("Users")
            .child(uid)
            .setValue(userMap)
            .addOnSuccessListener {
                // Notify user of success
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                // Redirect to user dashboard
                startActivity(Intent(this, UserDashboardActivity::class.java))
                finish() // Close registration screen
            }
            .addOnFailureListener { e -> // Handle any database write errors (Google, 2024)
                btnRegister.isEnabled = true
                btnRegister.text = "Register"
                Log.e(TAG, "Failed to save user", e)
                Toast.makeText(
                    this,
                    "Failed to save user data: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}

/*
Reference List

Android Developers, 2024. Firebase Authentication Overview. [online] Available at: https://firebase.google.com/docs/auth/android/start [Accessed 14 August 2025].

Google, 2024. Firebase Error Handling and Exceptions. [online] Available at: https://firebase.google.com/docs/reference/android/com/google/firebase/FirebaseException [Accessed 15 August 2025].

Kotlinlang.org, 2024. Kotlin Android Development Best Practices. [online] Available at: https://kotlinlang.org/docs/android-overview.html [Accessed 14 August 2025].

GeeksforGeeks, 2024. Implementing Login and Registration using Firebase in Android. [online] Available at: https://www.geeksforgeeks.org/login-and-registration-using-firebase-in-android/ [Accessed 15 August 2025].
 */
