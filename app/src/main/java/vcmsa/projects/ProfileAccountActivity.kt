package vcmsa.projects.fkj_consultants.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import vcmsa.projects.fkj_consultants.R
import java.util.*

class ProfileAccountActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_account)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        val firstNameInput = findViewById<EditText>(R.id.firstNameInput)
        val lastNameInput = findViewById<EditText>(R.id.lastNameInput)
        val usernameInput = findViewById<EditText>(R.id.usernameInput)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val addressInput = findViewById<EditText>(R.id.addressInput)
        val companyInput = findViewById<EditText>(R.id.companyInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val profilePictureUpload = findViewById<ImageView>(R.id.profilePictureUpload)

        loadUserProfile(firstNameInput, lastNameInput, usernameInput, emailInput, addressInput, companyInput)

        saveButton.setOnClickListener {
            val firstName = firstNameInput.text.toString().trim()
            val lastName = lastNameInput.text.toString().trim()
            val username = usernameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val address = addressInput.text.toString().trim()
            val company = companyInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (firstName.isBlank() || email.isBlank()) {
                Toast.makeText(this, "First Name and Email are required.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = auth.currentUser?.uid ?: return@setOnClickListener

            val userMap = hashMapOf<String, Any>(
                "firstName" to firstName,
                "lastName" to lastName,
                "username" to username,
                "email" to email,
                "address" to address,
                "company" to company
            )

            database.reference.child("users").child(userId)
                .setValue(userMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show()
                }

            if (password.isNotEmpty()) {
                auth.currentUser?.updatePassword(password)
                    ?.addOnFailureListener {
                        Toast.makeText(this, "Failed to update password.", Toast.LENGTH_SHORT).show()
                    }
            }

            if (selectedImageUri != null) {
                uploadProfilePicture(userId)
            }
        }

        profilePictureUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1001)
        }

        setupBottomNav()
    }

    private fun loadUserProfile(
        firstNameInput: EditText,
        lastNameInput: EditText,
        usernameInput: EditText,
        emailInput: EditText,
        addressInput: EditText,
        companyInput: EditText
    ) {
        val userId = auth.currentUser?.uid ?: return
        database.reference.child("users").child(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    firstNameInput.setText(snapshot.child("firstName").getValue(String::class.java))
                    lastNameInput.setText(snapshot.child("lastName").getValue(String::class.java))
                    usernameInput.setText(snapshot.child("username").getValue(String::class.java))
                    emailInput.setText(snapshot.child("email").getValue(String::class.java))
                    addressInput.setText(snapshot.child("address").getValue(String::class.java))
                    companyInput.setText(snapshot.child("company").getValue(String::class.java))
                }
            }
    }

    private fun uploadProfilePicture(userId: String) {
        val ref = storage.reference.child("profilePictures/$userId-${UUID.randomUUID()}.jpg")
        selectedImageUri?.let { uri ->
            ref.putFile(uri).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUrl ->
                    database.reference.child("users").child(userId)
                        .child("profilePictureUrl")
                        .setValue(downloadUrl.toString())
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            findViewById<ImageView>(R.id.profilePictureUpload).setImageURI(selectedImageUri)
        }
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, UserDashboardActivity::class.java))
                    true
                }
                R.id.nav_messages -> {
                    startActivity(Intent(this, ChatActivity::class.java))
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }
}
