package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.R.id


class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button

    companion object {
        private const val TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        etFirstName = findViewById(id.etFirstName)
        etLastName = findViewById(id.etLastName)
        etEmail = findViewById(id.etEmail)
        etPhone = findViewById(id.etPhone)
        etPassword = findViewById(id.etPassword)
        etConfirmPassword = findViewById(id.etConfirmPassword)
        btnRegister = findViewById(id.btnRegister)
    }

    private fun setupClickListeners() {
        btnRegister.setOnClickListener {
            if (validateInputs()) {
                registerUser()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        when {
            firstName.isEmpty() -> {
                etFirstName.error = "First name is required"
                etFirstName.requestFocus()
                return false
            }
            lastName.isEmpty() -> {
                etLastName.error = "Last name is required"
                etLastName.requestFocus()
                return false
            }
            email.isEmpty() -> {
                etEmail.error = "Email is required"
                etEmail.requestFocus()
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                etEmail.error = "Enter a valid email"
                etEmail.requestFocus()
                return false
            }
            phone.isEmpty() -> {
                etPhone.error = "Phone number is required"
                etPhone.requestFocus()
                return false
            }
            password.isEmpty() -> {
                etPassword.error = "Password is required"
                etPassword.requestFocus()
                return false
            }
            password.length < 6 -> {
                etPassword.error = "Password must be at least 6 characters"
                etPassword.requestFocus()
                return false
            }
            password != confirmPassword -> {
                etConfirmPassword.error = "Passwords do not match"
                etConfirmPassword.requestFocus()
                return false
            }
        }
        return true
    }

    private fun registerUser() {
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString()

        btnRegister.isEnabled = false
        btnRegister.text = "Registering..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let { firebaseUser ->
                        saveUserToDatabase(
                            firebaseUser.uid,
                            firstName,
                            lastName,
                            email,
                            phone
                        )
                    }
                } else {
                    btnRegister.isEnabled = true
                    btnRegister.text = "Register"
                    Log.e(TAG, "Registration failed", task.exception)
                    Toast.makeText(
                        this,
                        "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun saveUserToDatabase(
        uid: String,
        firstName: String,
        lastName: String,
        email: String,
        phone: String
    ) {
        val currentTime = System.currentTimeMillis()

        // Create user map with proper types
        val userMap = mapOf<String, Any>(
            "uid" to uid,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "phoneNumber" to phone,
            "role" to "user",
            "createdAt" to currentTime, // Long
            "lastLogin" to currentTime  // Long
        )

        val database = FirebaseDatabase.getInstance().getReference("Users")
        database.child(uid).setValue(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, UserDashboardActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
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

