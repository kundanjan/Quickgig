package com.example.quickgigapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit
import com.cometchat.chatuikit.shared.cometchatuikit.UIKitSettings
import com.example.quickgigapp.adapters.FreelancerAdapter
import com.example.quickgigapp.adapters.GigAdapter
import com.example.quickgigapp.databinding.ActivityMainBinding
import com.example.quickgigapp.models.FreelancerProfile
import com.example.quickgigapp.models.Gig
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity"

    private val appID = "2779777f159de927" // Replace with your App ID
    private val region = "in" // Replace with your App Region
    private val authKey = "060719aeb011474e980650ee84450dcf55d210f1" // Replace with your Auth Key

    private val uiKitSettings = UIKitSettings.UIKitSettingsBuilder()
        .setRegion(region)
        .setAppId(appID)
        .setAuthKey(authKey)
        .subscribePresenceForAllUsers()
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set theme before calling super.onCreate()
        setTheme(R.style.Theme_QuickGigApp)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeCometChat()
        checkUserRoleFromCometChat()
        setupBottomNavigation()
    }

    private fun initializeCometChat() {
        CometChatUIKit.init(this, uiKitSettings, object : CometChat.CallbackListener<String?>() {
            override fun onSuccess(successString: String?) {
                Log.d(TAG, "Initialization completed successfully")
//                Toast.makeText(this@MainActivity, "Initialization success!", Toast.LENGTH_SHORT).show()
            }
            override fun onError(e: CometChatException?) {
                Toast.makeText(this@MainActivity, "Initialization failed!", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Initialization failed: ${e?.message}")
            }
        })
    }

    private fun setupGigsList() {
        val database = FirebaseDatabase.getInstance().getReference("gigs")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val gigList = mutableListOf<Gig>()

                for (gigSnapshot in snapshot.children) {
                    val title = gigSnapshot.child("title").getValue(String::class.java) ?: ""
                    val description = gigSnapshot.child("description").getValue(String::class.java) ?: ""
                    val price = gigSnapshot.child("price").getValue(Int::class.java)?.toString() ?: ""
                    val clientUid = gigSnapshot.child("clientUid").getValue(String::class.java) ?: ""

                    gigList.add(Gig(title, description, price, clientUid))
                }

                binding.rvGigs.adapter = GigAdapter(gigList) { gig ->
                    openClientChat(gig.clientUid)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error fetching gigs", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkUserRoleFromCometChat() {
        val currentUser = CometChat.getLoggedInUser()

        if (currentUser != null) {
            val role = currentUser.role

            Log.d(TAG, "User role from CometChat: $role")

            when (role.toString().lowercase()) {
                "cl" -> {
                    Log.d(TAG, "Current user is CLIENT - Loading freelancer profiles")
                    val TextView = findViewById<TextView>(R.id.tv_title)
                    TextView.text = "Freelancers"
                    loadFreelancerProfiles()

                }
                "fr" -> {
                    Log.d(TAG, "Current user is FREELANCER - Loading gigs")
                    setupGigsList()
                }
                else -> {
                    Log.d(TAG, "Unknown or missing role: $role, defaulting to gigs")
                    setupGigsList()
                }
            }
        } else {
            Log.e(TAG, "No logged in CometChat user found")
            setupGigsList()
        }
    }

    private fun loadFreelancerProfiles() {
        val database = FirebaseDatabase.getInstance().getReference("users")

        database.orderByChild("role").equalTo("Freelancer")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val freelancers = mutableListOf<FreelancerProfile>()

                    for (userSnapshot in snapshot.children) {
                        try {
                            // Use Firebase's built-in getValue with the data class
                            val freelancerProfile = userSnapshot.getValue(FreelancerProfile::class.java)

                            if (freelancerProfile != null) {
                                // Set the uid from the key since it might not be stored in the object
                                val uid = userSnapshot.key ?: freelancerProfile.uid
                                val updatedProfile = freelancerProfile.copy(uid = uid)

                                freelancers.add(updatedProfile)
                                Log.d(TAG, "Loaded freelancer: ${updatedProfile.name} with ${updatedProfile.skills?.size ?: 0} skills")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing freelancer profile: ${e.message}")

                            // Fallback to manual parsing if direct conversion fails
                            val uid = userSnapshot.key ?: ""
                            val name = userSnapshot.child("name").getValue(String::class.java) ?: "Unnamed"
                            val email = userSnapshot.child("email").getValue(String::class.java) ?: "No email"
                            val role = userSnapshot.child("role").getValue(String::class.java) ?: "Freelancer"
                            val createdAt = userSnapshot.child("createdAt").getValue(Long::class.java) ?: 0L

                            // Handle skills - check if it's stored as a list
                            val skills = mutableListOf<String>()
                            val skillsSnapshot = userSnapshot.child("skills")

                            if (skillsSnapshot.exists()) {
                                if (skillsSnapshot.hasChildren()) {
                                    // Skills stored as array/list
                                    for (skillSnapshot in skillsSnapshot.children) {
                                        val skill = skillSnapshot.getValue(String::class.java)
                                        if (!skill.isNullOrBlank()) {
                                            skills.add(skill.trim())
                                        }
                                    }
                                } else {
                                    // Skills stored as comma-separated string
                                    val skillsString = skillsSnapshot.getValue(String::class.java)
                                    if (!skillsString.isNullOrBlank()) {
                                        skills.addAll(
                                            skillsString.split(",")
                                                .map { it.trim() }
                                                .filter { it.isNotBlank() }
                                        )
                                    }
                                }
                            }

                            val manualProfile = FreelancerProfile(
                                uid = uid,
                                name = name,
                                email = email,
                                role = role,
                                skills = if (skills.isNotEmpty()) skills else null,
                                createdAt = createdAt
                            )

                            freelancers.add(manualProfile)
                            Log.d(TAG, "Manually parsed freelancer: ${manualProfile.name} with ${manualProfile.skills?.size ?: 0} skills")
                        }
                    }

                    Log.d(TAG, "Loaded ${freelancers.size} freelancers")
                    binding.rvGigs.adapter = FreelancerAdapter(freelancers) { freelancer ->
                        openClientChat(freelancer.uid)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity, "Error loading freelancers", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Database error: ${error.message}")
                }
            })
    }

    private fun openClientChat(uid: String) {
        val intent = Intent(this, MessageActivity::class.java)
        intent.putExtra("uid", uid)
        intent.putExtra("conversationType", "user")
        startActivity(intent)
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Already on Home, no action needed
                    true
                }
                R.id.navigation_chat -> {
                    startActivity(Intent(this, ConversationActivity::class.java))
                    true
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}