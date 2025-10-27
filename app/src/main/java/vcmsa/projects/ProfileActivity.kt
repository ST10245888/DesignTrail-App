package student.projects.fkj_consultants_app.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import student.projects.fkj_consultants_app.R

class ProfileActivity : AppCompatActivity() {

    // Firebase authentication instance
    private lateinit var auth: FirebaseAuth

    // Firebase Database reference pointing to "Users" node
    private val database = FirebaseDatabase.getInstance().getReference("Users")

    // Firebase Storage reference for storing profile pictures
    private val storageRef = FirebaseStorage.getInstance().getReference("ProfilePictures")

    // UI components for profile picture and settings
    private lateinit var ivProfilePicture: ImageView
    private lateinit var ivEditProfilePicture: ImageView
    private lateinit var switchNotifications: Switch
    private lateinit var btnChatOptions: LinearLayout
    private lateinit var btnInviteFriends: LinearLayout
    private lateinit var btnHelpFeedback: LinearLayout
    private lateinit var btnLogout: LinearLayout

    // Request code for selecting image from gallery
    private val PICK_IMAGE_REQUEST = 1001

    // Stores selected image URI temporarily
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return // Get current user ID or exit if null

        // Initialize UI Views
        val etFirstName = findViewById<EditText>(R.id.etFirstName)
        val etLastName = findViewById<EditText>(R.id.etLastName)
        val etUserName = findViewById<EditText>(R.id.etUserName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etAddress = findViewById<EditText>(R.id.etAddress)
        val etPhoneNumber = findViewById<EditText>(R.id.etPhoneNumber)
        val btnUpdateProfile = findViewById<Button>(R.id.btnUpdateProfile)
        val btnResetPassword = findViewById<Button>(R.id.btnResetPassword)
        ivProfilePicture = findViewById(R.id.ivProfilePicture)
        ivEditProfilePicture = findViewById(R.id.ivEditProfilePicture)

        // New Settings Views
        switchNotifications = findViewById(R.id.switchNotifications)
        btnChatOptions = findViewById(R.id.btnChatOptions)
        btnInviteFriends = findViewById(R.id.btnInviteFriends)
        btnHelpFeedback = findViewById(R.id.btnHelpFeedback)
        btnLogout = findViewById(R.id.btnLogout)

        // Load Current User Data from Firebase
        database.child(userId).get().addOnSuccessListener { snapshot ->
            // Retrieve user details from Firebase Realtime Database
            etFirstName.setText(snapshot.child("firstName").value?.toString() ?: "")
            etLastName.setText(snapshot.child("lastName").value?.toString() ?: "")
            etUserName.setText(snapshot.child("userName").value?.toString() ?: "")
            etEmail.setText(snapshot.child("Email").value?.toString() ?: "")
            etAddress.setText(snapshot.child("Address").value?.toString() ?: "")
            etPhoneNumber.setText(snapshot.child("phoneNumber").value?.toString() ?: "")

            // Load and display user's profile picture using Glide
            val profileUrl = snapshot.child("profilePictureUrl").value?.toString()
            if (!profileUrl.isNullOrEmpty()) {
                Glide.with(this).load(profileUrl).into(ivProfilePicture)
            }

            // Load and set notification switch state
            val notificationsEnabled = snapshot.child("notificationsEnabled").value?.toString()?.toBoolean() ?: true
            switchNotifications.isChecked = notificationsEnabled
        }

        // Update Profile Button Logic
        btnUpdateProfile.setOnClickListener {
            // Get input values from text fields
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val userName = etUserName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val address = etAddress.text.toString().trim()
            val phoneNumber = etPhoneNumber.text.toString().trim()

            // Validate required fields
            if (firstName.isEmpty() || lastName.isEmpty() || userName.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate email format
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Prepare data to update
            val updates = mutableMapOf<String, Any>(
                "firstName" to firstName,
                "lastName" to lastName,
                "userName" to userName,
                "Email" to email,
                "Address" to address,
                "phoneNumber" to phoneNumber,
                "notificationsEnabled" to switchNotifications.isChecked
            )

            // Upload image if user selected a new one
            if (imageUri != null) {
                val photoRef = storageRef.child("$userId.jpg")
                photoRef.putFile(imageUri!!)
                    .addOnSuccessListener {
                        // Get the download URL of uploaded image and update profile
                        photoRef.downloadUrl.addOnSuccessListener { uri ->
                            updates["profilePictureUrl"] = uri.toString()
                            updateUserProfile(userId, updates, email)
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Image upload failed: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                // No new image; update only text information
                updateUserProfile(userId, updates, email)
            }
        }

        // Password Reset Button Logic
        btnResetPassword.setOnClickListener {
            val currentEmail = auth.currentUser?.email ?: return@setOnClickListener
            auth.sendPasswordResetEmail(currentEmail)
                .addOnSuccessListener {
                    Toast.makeText(this, "Password reset email sent to $currentEmail", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to send reset email: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        // Profile Picture Edit Button
        ivEditProfilePicture.setOnClickListener {
            // Open gallery for image selection
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Notifications Switch Logic
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            // Save user's notification preference in database
            database.child(userId).child("notificationsEnabled").setValue(isChecked)
            val msg = if (isChecked) "Notifications Enabled" else "Notifications Disabled"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // Chat Options (Feature Coming Soon)
        btnChatOptions.setOnClickListener {
            Snackbar.make(findViewById(android.R.id.content), "Chat settings will be available soon.", Snackbar.LENGTH_LONG).show()
        }

        // Invite Friends Feature
        btnInviteFriends.setOnClickListener {
            // Create a share intent to invite friends via any supported app
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Join FKJ Consultants App")
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Hey! Check out the FKJ Consultants App — it’s amazing! Download it here: https://play.google.com")
            startActivity(Intent.createChooser(shareIntent, "Invite via"))
        }

        // Help & Feedback Feature
        btnHelpFeedback.setOnClickListener {
            // Compose an email to support team
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = Uri.parse("mailto:")
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("support@fkjconsultants.com"))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Help & Feedback")
            startActivity(Intent.createChooser(emailIntent, "Send Feedback"))
        }

        // Logout Feature
        btnLogout.setOnClickListener {
            // Sign out from Firebase Authentication
            auth.signOut()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            // Redirect back to login screen
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // Function to Update User Profile in Firebase
    private fun updateUserProfile(userId: String, updates: Map<String, Any>, email: String) {
        // Update user data in Realtime Database
        database.child(userId).updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
            }

        // Update user email in Firebase Authentication
        auth.currentUser?.updateEmail(email)
            ?.addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update email: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // Handle Image Selection Result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // If the result is from image picker and successful, display chosen image
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            Glide.with(this).load(imageUri).into(ivProfilePicture)
        }
    }
}
