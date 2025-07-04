package com.example.quickgigapp

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import android.view.ViewGroup


class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    // UI Elements
    private lateinit var profileImageView: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var bioTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var roleTextView: TextView
    private lateinit var memberSinceTextView: TextView
    private lateinit var detailsCardView: CardView
    private lateinit var createGigButton: MaterialButton

    // User data
    private var currentUserUid: String? = null
    private var userRole: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Initialize UI elements
        initializeViews()

        // Get current user
        currentUserUid = auth.currentUser?.uid

        if (currentUserUid != null) {
            displayFirebaseUserData()
            fetchUserRoleFromCometChat()
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Set up button click listener
        createGigButton.setOnClickListener {
            showCreateGigDialog()
        }
        val logoutButton = findViewById<MaterialButton>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            logoutUser()
        }

    }
    private fun logoutUser() {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut()

        // Logout from CometChat
        CometChat.logout(object : CometChat.CallbackListener<String>() {
            override fun onSuccess(p0: String?) {
                Toast.makeText(this@ProfileActivity, "Logged out successfully", Toast.LENGTH_SHORT).show()
                // Navigate to LoginActivity and clear the back stack
                val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }

            override fun onError(e: CometChatException?) {
                Toast.makeText(this@ProfileActivity, "CometChat logout failed: ${e?.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun initializeViews() {
        profileImageView = findViewById(R.id.profileImageView)
        nameTextView = findViewById(R.id.nameTextView)
        bioTextView = findViewById(R.id.bioTextView)
        emailTextView = findViewById(R.id.emailTextView)
        roleTextView = findViewById(R.id.roleTextView)
        memberSinceTextView = findViewById(R.id.memberSinceTextView)
        detailsCardView = findViewById(R.id.detailsCardView)
        createGigButton = findViewById(R.id.createGigButton)

        // Initially hide the create gig button
        createGigButton.visibility = View.GONE
    }

    private fun displayFirebaseUserData() {
        val user = auth.currentUser
        user?.let {
            // Display available Firebase Auth data
            val displayName = it.displayName ?: "User"
            val email = it.email ?: "No email"
            val uid = it.uid
            val creationTimestamp = it.metadata?.creationTimestamp
            val lastSignInTimestamp = it.metadata?.lastSignInTimestamp

            // Update UI with Firebase Auth data
            nameTextView.text = displayName
            bioTextView.text = "Welcome to QuickGig!"
            emailTextView.text = email

            // Format creation date
            creationTimestamp?.let { timestamp ->
                memberSinceTextView.text = formatMemberSinceDate(timestamp)
            } ?: run {
                memberSinceTextView.text = "Recently joined"
            }


            Log.d("ProfileActivity", "User UID: $uid")
            Log.d("ProfileActivity", "Display Name: $displayName")
            Log.d("ProfileActivity", "Email: $email")
            Log.d("ProfileActivity", "Created: ${Date(creationTimestamp ?: 0)}")
            Log.d("ProfileActivity", "Last Sign In: ${Date(lastSignInTimestamp ?: 0)}")
        }
    }

    private fun fetchUserRoleFromCometChat() {
        val user = CometChat.getLoggedInUser()
        if (user == null) {
            Log.e("ProfileActivity", "User not logged in to CometChat")
            roleTextView.text = "Not logged in to chat"
            return
        }
        nameTextView.text = user.name

        // Extract role from metadata or tags
        userRole = user.role

        Log.d("ProfileActivity", "User role from CometChat: $userRole")

        // Update UI based on role
        runOnUiThread {
            if (userRole == "cl") {
                createGigButton.visibility = View.VISIBLE
                roleTextView.text = "Client"
            } else {
                createGigButton.visibility = View.GONE
                roleTextView.text = userRole?.replaceFirstChar { it.uppercaseChar() } ?: "Freelancer"
            }
        }
    }


    private fun formatMemberSinceDate(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return format.format(date)
    }

    private fun showCreateGigDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_create_gig)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Initialize dialog views
        val titleEditText = dialog.findViewById<EditText>(R.id.gigTitleEditText)
        val descriptionEditText = dialog.findViewById<EditText>(R.id.gigDescriptionEditText)
        val priceEditText = dialog.findViewById<EditText>(R.id.gigPriceEditText)
        val createButton = dialog.findViewById<Button>(R.id.createGigDialogButton)
        val cancelButton = dialog.findViewById<Button>(R.id.cancelGigDialogButton)

        // Pre-fill with default values
        titleEditText.setText("Logo Design")
        descriptionEditText.setText("Create a professional logo for your brand")
        priceEditText.setText("50")

        createButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()
            val priceText = priceEditText.text.toString().trim()

            if (title.isEmpty() || description.isEmpty() || priceText.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val price = priceText.toDouble()
                createGig(title, description, price)
                dialog.dismiss()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun createGig(title: String, description: String, price: Double) {
        currentUserUid?.let { uid ->
            val gigId = database.child("gigs").push().key

            if (gigId != null) {
                val gigData = mapOf(
                    "gigId" to gigId,
                    "title" to title,
                    "description" to description,
                    "price" to price,
                    "clientUid" to uid,
                    "clientEmail" to auth.currentUser?.email,
                    "clientName" to auth.currentUser?.displayName,
                    "status" to "active",
                    "createdAt" to System.currentTimeMillis()
                )

                database.child("gigs").child(gigId).setValue(gigData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Gig created successfully!", Toast.LENGTH_SHORT).show()
                        Log.d("ProfileActivity", "Gig created with ID: $gigId")
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Failed to create gig", Toast.LENGTH_SHORT).show()
                        Log.e("ProfileActivity", "Failed to create gig: ${exception.message}")
                    }
            }
        }
    }
}