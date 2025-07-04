package com.example.quickgigapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.quickgigapp.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.User
import com.cometchat.chat.exceptions.CometChatException

class SignupActivity : ComponentActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val TAG = "SignupActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupClickListeners()
        setupRoleChangeListener()
    }

    private fun setupRoleChangeListener() {
        binding.radioGroupRole.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.radioFreelancer.id -> {
                    binding.tilSkills.visibility = View.VISIBLE
                }
                binding.radioClient.id -> {
                    binding.tilSkills.visibility = View.GONE
                    binding.etSkills.text?.clear()
                }
            }
        }
    }

    private fun getSelectedRole(): String {
        return when {
            binding.radioClient.isChecked -> "Client"
            binding.radioFreelancer.isChecked -> "Freelancer"
            else -> ""
        }
    }

    private fun getRoleId(displayRole: String): String {
        return when (displayRole.lowercase()) {
            "client" -> "cl"
            "freelancer" -> "fr"
            else -> "default"
        }
    }

    private fun setupClickListeners() {
        binding.btnSignup.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            val selectedRole = getSelectedRole()
            val skills = binding.etSkills.text.toString().trim()

            Log.d(TAG, "Selected role: $selectedRole")

            if (validateInput(name, email, password, confirmPassword, selectedRole, skills)) {
                signupUser(name, email, password, selectedRole, skills)
            }
        }

        binding.tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun validateInput(name: String, email: String, password: String, confirmPassword: String, role: String, skills: String): Boolean {
        return when {
            name.isEmpty() -> {
                binding.tilName.error = "Name is required"
                false
            }
            email.isEmpty() -> {
                binding.tilEmail.error = "Email is required"
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.tilEmail.error = "Please enter a valid email address"
                false
            }
            password.isEmpty() -> {
                binding.tilPassword.error = "Password is required"
                false
            }
            password.length < 6 -> {
                binding.tilPassword.error = "Password must be at least 6 characters"
                false
            }
            confirmPassword.isEmpty() -> {
                binding.tilConfirmPassword.error = "Please confirm your password"
                false
            }
            password != confirmPassword -> {
                binding.tilConfirmPassword.error = "Passwords do not match"
                false
            }
            role.isEmpty() -> {
                Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show()
                false
            }
            role == "Freelancer" && skills.isEmpty() -> {
                binding.tilSkills.error = "Please enter your skills"
                false
            }
            else -> {
                binding.tilName.error = null
                binding.tilEmail.error = null
                binding.tilPassword.error = null
                binding.tilConfirmPassword.error = null
                binding.tilSkills.error = null
                true
            }
        }
    }

    private fun signupUser(name: String, email: String, password: String, role: String, skills: String) {
        binding.btnSignup.isEnabled = false
        binding.btnSignup.text = "Creating Account..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        createCometChatUser(it.uid, name, email, role, skills)
                    }
                } else {
                    binding.btnSignup.isEnabled = true
                    binding.btnSignup.text = "Sign Up"

                    val errorMessage = when (task.exception?.message) {
                        "The email address is already in use by another account." -> "This email is already registered."
                        "The email address is badly formatted." -> "Please enter a valid email address."
                        "The given password is invalid. [ Password should be at least 6 characters ]" -> "Password must be at least 6 characters."
                        else -> "Signup failed: ${task.exception?.message}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun createCometChatUser(uid: String, name: String, email: String, role: String, skills: String) {
        val apiKey = "060719aeb011474e980650ee84450dcf55d210f1"

        val cometChatUser = User().apply {
            this.uid = uid
            this.name = name
            this.role = getRoleId(role)
        }

        CometChat.createUser(cometChatUser, apiKey, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(cometChatUser: User) {
                saveUserToDatabase(uid, name, email, role, skills)
            }

            override fun onError(exception: CometChatException) {
                Log.e(TAG, "CometChat user creation failed: ${exception.message}")

                auth.currentUser?.delete()?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d(TAG, "Firebase user deleted due to CometChat failure")
                    }
                }

                binding.btnSignup.isEnabled = true
                binding.btnSignup.text = "Sign Up"

                val errorMessage = when (exception.code) {
                    "ERR_ALREADY_EXISTS" -> "Chat user already exists. Try logging in."
                    "ERR_INVALID_API_KEY" -> "Invalid chat API key."
                    else -> "Failed to create chat user: ${exception.message}"
                }

                Toast.makeText(this@SignupActivity, errorMessage, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun saveUserToDatabase(uid: String, name: String, email: String, role: String, skills: String) {
        val skillsList = if (role == "Freelancer" && skills.isNotEmpty()) {
            skills.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        } else {
            emptyList()
        }

        val userData = mapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "role" to role,
            "skills" to skillsList,
            "createdAt" to System.currentTimeMillis()
        )

        database.reference.child("users").child(uid).setValue(userData)
            .addOnSuccessListener {
                binding.btnSignup.isEnabled = true
                binding.btnSignup.text = "Sign Up"

                Toast.makeText(this@SignupActivity, "Account created successfully!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                finish()
            }
            .addOnFailureListener { exception ->
                binding.btnSignup.isEnabled = true
                binding.btnSignup.text = "Sign Up"

                Toast.makeText(this@SignupActivity, "Failed to save user: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }
}