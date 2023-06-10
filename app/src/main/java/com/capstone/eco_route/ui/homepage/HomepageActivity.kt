package com.capstone.eco_route.ui.homepage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.capstone.eco_route.R
import com.capstone.eco_route.databinding.ActivityHomepageBinding
import com.capstone.eco_route.ui.login.LoginActivity
import com.capstone.eco_route.ui.profile.ProfileActivity
import com.google.firebase.auth.FirebaseAuth

class HomepageActivity : AppCompatActivity() {

    private val binding: ActivityHomepageBinding by lazy {
        ActivityHomepageBinding.inflate(layoutInflater)
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
            this@HomepageActivity,
            LoginActivity::class.java
        ))
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_bottom_action, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.action_profile -> {
                val intent = Intent(
                    this@HomepageActivity,
                    LoginActivity::class.java
                )
                startActivity(intent)
            }

        }

        return super.onOptionsItemSelected(item)
    }
}