package com.example.quickgigapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit
import com.cometchat.chatuikit.shared.cometchatuikit.UIKitSettings
import com.example.quickgigapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : ComponentActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
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
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        initializeCometChat()
        checkLoggedInUser()


        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvSignupLink.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

    }
    private fun checkLoggedInUser() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // Check if CometChat user session is active
            CometChat.getLoggedInUser()?.let {
                // ✅ Already logged in to CometChat as well
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } ?: run {
                // 👇 Firebase logged in but CometChat session expired - try re-login
                CometChat.login(currentUser.uid, authKey, object : CometChat.CallbackListener<com.cometchat.chat.models.User>() {
                    override fun onSuccess(user: com.cometchat.chat.models.User?) {
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }

                    override fun onError(e: CometChatException?) {
                        Log.e("LoginActivity", "Auto-login CometChat failed: ${e?.message}")
                        Toast.makeText(this@LoginActivity, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }


    private fun initializeCometChat() {
        CometChatUIKit.init(this, uiKitSettings, object : CometChat.CallbackListener<String?>() {
            override fun onSuccess(successString: String?) {
                Log.d("loginactivity", "Initialization completed successfully")
                Toast.makeText(this@LoginActivity, "Initialization success!", Toast.LENGTH_SHORT).show()
            }
            override fun onError(e: CometChatException?) {
                Toast.makeText(this@LoginActivity, "Initialization failed!", Toast.LENGTH_SHORT).show()
                Log.e("loginactivity", "Initialization failed: ${e?.message}")
            }
        })
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val uid = it.uid
                        // Login to CometChat using the Firebase UID
                        CometChat.login(uid, authKey, object : CometChat.CallbackListener<com.cometchat.chat.models.User>() {
                            override fun onSuccess(cometChatUser: com.cometchat.chat.models.User) {
                                Log.d("loginactivity", "CometChat login successful: ${cometChatUser.uid}")
                                Toast.makeText(this@LoginActivity, "Logged in to CometChat as ${cometChatUser.name}", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finish()
                            }

                            override fun onError(e: CometChatException) {
                                Log.e("loginactivity", "CometChat login failed: ${e.message}")
                                Toast.makeText(this@LoginActivity, "CometChat login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}