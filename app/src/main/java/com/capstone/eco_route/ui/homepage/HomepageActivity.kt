package com.capstone.eco_route.ui.homepage

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.capstone.eco_route.R
import com.capstone.eco_route.databinding.ActivityHomepageBinding
import com.capstone.eco_route.datasource.db.other.ContantsToken.SHOW_ACTION_TRACKER_ACTIVITY
import com.capstone.eco_route.ui.detailtracker.DetailTrackerActivity
import com.capstone.eco_route.ui.login.LoginActivity
import com.capstone.eco_route.ui.tracker.TrackerActivity
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

        pendingIntentTrackerActivity(intent)


        binding.logoutButton.setOnClickListener {
            userLogout()
        }

        binding.buttonCalculate.setOnClickListener {
            AlertDialog.Builder(
                this@HomepageActivity
            ).apply {
                setTitle("Calculate Option")
                setMessage("Are you want to calculate manual?")

                setPositiveButton("Yes") { _, _ ->
                    val intent = Intent(
                        this@HomepageActivity,
                        DetailTrackerActivity::class.java
                    )
                    startActivity(intent)
                }
                setNegativeButton("No") { _, _ ->
                    val intent = Intent(
                        this@HomepageActivity,
                        TrackerActivity::class.java
                    )
                    startActivity(intent)
                }
                create()
                show()

            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        pendingIntentTrackerActivity(intent)
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

    private fun pendingIntentTrackerActivity(intent: Intent?) {
        if (intent?.action == SHOW_ACTION_TRACKER_ACTIVITY) {
            val intentTracker = Intent(
                this@HomepageActivity,
                TrackerActivity::class.java
            )
            startActivity(intentTracker)
        }
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