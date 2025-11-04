package vcmsa.projects.fkj_consultants.activities

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
import vcmsa.projects.fkj_consultants.R

class ProfileActivity : AppCompatActivity() {

    // Firebase authentication instance (Firebase, 2024)
    private lateinit var auth: FirebaseAuth

    // Firebase Database reference pointing to "Users" node
    private val database = FirebaseDatabase.getInstance().getReference("Users")

    // Firebase Storage reference for storing profile pictures (Firebase, 2024)
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

        // Initialize FirebaseAuth (Firebase, 2024)
        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return // Get current user ID or exit if null

        // Initialize UI Views (Android Developers, 2024)
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

        // New Settings Views (Android Developers, 2024)
        switchNotifications = findViewById(R.id.switchNotifications)
        btnChatOptions = findViewById(R.id.btnChatOptions)
        btnInviteFriends = findViewById(R.id.btnInviteFriends)
        btnHelpFeedback = findViewById(R.id.btnHelpFeedback)
        btnLogout = findViewById(R.id.btnLogout)

        // Load Current User Data from Firebase (Firebase, 2024)
        database.child(userId).get()
            .addOnSuccessListener { snapshot ->
                try {
                    // Retrieve user details from Firebase Realtime Database
                    etFirstName.setText(snapshot.child("firstName").value?.toString() ?: "")
                    etLastName.setText(snapshot.child("lastName").value?.toString() ?: "")
                    etUserName.setText(snapshot.child("userName").value?.toString() ?: "")
                    etEmail.setText(snapshot.child("Email").value?.toString() ?: "")
                    etAddress.setText(snapshot.child("Address").value?.toString() ?: "")

                    // Handle phone number: set +27 prefix if empty (Android Developers, 2024)
                    val phoneValue = snapshot.child("phoneNumber").value?.toString() ?: ""
                    if (phoneValue.isEmpty()) {
                        etPhoneNumber.setText("+27 ")
                    } else {
                        etPhoneNumber.setText(phoneValue)
                    }
                    etPhoneNumber.setSelection(etPhoneNumber.text.length) // Cursor to end

                    // Add TextWatcher to enforce "+27 " prefix and digit-only formatting (Android Developers, 2024)
                    etPhoneNumber.addTextChangedListener(object : android.text.TextWatcher {
                        private var isEditing = false

                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                        override fun afterTextChanged(s: android.text.Editable?) {
                            if (isEditing) return
                            isEditing = true

                            val text = s.toString()

                            // Ensure text starts with "+27 "
                            if (!text.startsWith("+27 ")) {
                                etPhoneNumber.setText("+27 ")
                                etPhoneNumber.setSelection(etPhoneNumber.text.length)
                                isEditing = false
                                return
                            }

                            // Remove all non-digit chars after "+27 "
                            val digitsPart = text.substring(4).filter { it.isDigit() }

                            // Format: +27 XX XXX XXXX (spaces after 2nd and 5th digit)
                            val formatted = buildString {
                                append("+27 ")
                                digitsPart.forEachIndexed { index, ch ->
                                    append(ch)
                                    if (index == 1 || index == 4) append(" ")
                                }
                            }

                            if (formatted != text) {
                                etPhoneNumber.setText(formatted)
                                etPhoneNumber.setSelection(etPhoneNumber.text.length)
                            }

                            isEditing = false
                        }
                    })

                    // Load and display user's profile picture using Glide (Bumptech, 2024)
                    val profileUrl = snapshot.child("profilePictureUrl").value?.toString()
                    if (!profileUrl.isNullOrEmpty()) {
                        Glide.with(this).load(profileUrl).into(ivProfilePicture)
                    }

                    // Load and set notification switch state
                    val notificationsEnabled = snapshot.child("notificationsEnabled").value?.toString()?.toBoolean() ?: true
                    switchNotifications.isChecked = notificationsEnabled
                } catch (e: Exception) {
                    Toast.makeText(this, "Failed to load profile data: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load profile: ${e.message}", Toast.LENGTH_LONG).show()
            }

        // Update Profile Button Logic (Android Developers, 2024)
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

            // Validate phone number format: must be exactly "+27 XX XXX XXXX"
            val phonePattern = Regex("""^\+27 \d{2} \d{3} \d{4}$""")
            if (!phonePattern.matches(phoneNumber)) {
                Toast.makeText(this, "Phone number must be in format: +27 XX XXX XXXX", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Prepare data to update (Firebase, 2024)
            val updates = mutableMapOf<String, Any>(
                "firstName" to firstName,
                "lastName" to lastName,
                "userName" to userName,
                "Email" to email,
                "Address" to address,
                "phoneNumber" to phoneNumber,
                "notificationsEnabled" to switchNotifications.isChecked
            )

            // Upload image if user selected a new one (Bumptech, 2024)
            if (imageUri != null) {
                val photoRef = storageRef.child("$userId.jpg")
                photoRef.putFile(imageUri!!)
                    .addOnSuccessListener {
                        // Get the download URL of uploaded image and update profile (Bumptech, 2024)
                        photoRef.downloadUrl
                            .addOnSuccessListener { uri ->
                                updates["profilePictureUrl"] = uri.toString()
                                updateUserProfile(userId, updates, email)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to get image URL: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Image upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                // No new image; update only text information
                updateUserProfile(userId, updates, email)
            }
        }

        // Password Reset Button Logic (Firebase, 2024)
        btnResetPassword.setOnClickListener {
            val currentEmail = auth.currentUser?.email
            if (currentEmail == null) {
                Toast.makeText(this, "No authenticated user found.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
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
            // Open gallery for image selection (Bumptech, 2024)
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Notifications Switch Logic (Android Developers, 2024)
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            // Save user's notification preference in database (Firebase, 2024)
            database.child(userId).child("notificationsEnabled").setValue(isChecked)
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update notifications: ${e.message}", Toast.LENGTH_LONG).show()
                }
            val msg = if (isChecked) "Notifications Enabled" else "Notifications Disabled"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // Updated Chat Options Button Logic to open ChatActivity (Firebase, 2024)
        btnChatOptions.setOnClickListener {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUserEmail = currentUser.email ?: ""

            // List of admin emails (Firebase, 2024)
            val adminEmails = listOf(
                "kush@gmail.com",
                "keitumetse01@gmail.com",
                "malikaOlivia@gmail.com",
                "JamesJameson@gmail.com"
            )

            val isAdmin = adminEmails.contains(currentUserEmail)

            val intent = Intent(this, vcmsa.projects.fkj_consultants.activities.ChatActivity::class.java)

            if (isAdmin) {
                // For admins, ideally let them select a user to chat with (Firebase, 2024)
                Toast.makeText(this, "Please select a user to chat with (feature to be implemented)", Toast.LENGTH_LONG).show()
                // Passing own email as placeholder target user
                intent.putExtra("targetUserEmail", currentUserEmail)
                startActivity(intent)
            } else {
                // Regular user opens their chat
                startActivity(intent)
            }
        }

        // Invite Friends Feature (Android Developers, 2024)
        btnInviteFriends.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Join FKJ Consultants App")
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Hey! Check out the FKJ Consultants App — it’s amazing! Download it here: https://play.google.com")
            startActivity(Intent.createChooser(shareIntent, "Invite via"))
        }

        // Help & Feedback Feature (Android Developers, 2024)
        btnHelpFeedback.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = Uri.parse("mailto:")
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("support@fkjconsultants.com"))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Help & Feedback")
            startActivity(Intent.createChooser(emailIntent, "Send Feedback"))
        }

        // Logout Feature (Firebase, 2024)
        btnLogout.setOnClickListener {
            try {
                auth.signOut()
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } catch (e: Exception) {
                Toast.makeText(this, "Logout failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Function to Update User Profile in Firebase (Firebase, 2024)
    private fun updateUserProfile(userId: String, updates: Map<String, Any>, email: String) {
        database.child(userId).updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
            }

        auth.currentUser?.updateEmail(email)
            ?.addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update email: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // Handle Image Selection Result (Android Developers, 2024)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
                imageUri = data.data
                Glide.with(this).load(imageUri).into(ivProfilePicture)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to load selected image: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

/*
Reference List

Android Developers, 2024. AppCompatActivity | Android Developers. Google. [online]. Available at: https://developer.android.com/reference/androidx/appcompat/app/AppCompatActivity [Accessed 10 August 2025]

Firebase, 2024. Firebase Authentication Documentation. Google Firebase. [online]. Available at: https://firebase.google.com/docs/auth [Accessed 10 August 2025]

Firebase, 2024. Firebase Realtime Database Documentation. Google Firebase. [online]. Available at: https://firebase.google.com/docs/database [Accessed 10 August 2025]

Firebase, 2024. Firebase Storage Documentation. Google Firebase. [online]. Available at: https://firebase.google.com/docs/storage [Accessed 10 August 2025]

Bumptech, 2024. Glide Image Loading Library for Android. GitHub Repository. [online]. Available at: https://github.com/bumptech/glide [Accessed 10 August 2025]

 */
