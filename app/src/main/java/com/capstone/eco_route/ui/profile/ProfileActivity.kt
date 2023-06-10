package com.capstone.eco_route.ui.profile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.capstone.eco_route.R
import com.capstone.eco_route.databinding.ActivityProfileBinding
import com.capstone.eco_route.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    private val binding: ActivityProfileBinding by lazy {
        ActivityProfileBinding.inflate(layoutInflater)
    }

    private lateinit var authentication: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        authentication = FirebaseAuth.getInstance()

        binding.logoutButton.setOnClickListener {
            userLogout()
        }

    }

    private fun userLogout() {
        authentication.signOut()
        startActivity(Intent(
            this@ProfileActivity,
            LoginActivity::class.java
        ))
        finish()
    }
}