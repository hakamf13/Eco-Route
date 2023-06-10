package com.capstone.eco_route.ui.register

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.capstone.eco_route.databinding.ActivityRegisterBinding
import com.capstone.eco_route.ui.homepage.HomepageActivity
import com.capstone.eco_route.ui.login.LoginActivity
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.klinker.android.link_builder.Link
import com.klinker.android.link_builder.applyLinks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private val binding: ActivityRegisterBinding by lazy {
        ActivityRegisterBinding.inflate(layoutInflater)
    }
    private lateinit var authentication: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        authentication = FirebaseAuth.getInstance()

        binding.registerButton.setOnClickListener {
            registerWithEmail()
        }

        linkSetup()

    }

    private fun registerWithEmail() {
        val userEmail = binding.customEmail.text.toString()
        val userPassword = binding.customPassword.text.toString()
        val userName = binding.customName.text.toString()

        if (userEmail.isNotEmpty() && userPassword.isNotEmpty() && userName.isNotEmpty()) {
            /*userRegisterWithEmail(userEmail, userPassword, userName)*/
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    authentication.createUserWithEmailAndPassword(userEmail, userPassword).await()
                    withContext(Dispatchers.Main) {
                        logStateValidate(currentUser = null)
                    }
                } catch (e: ApiException) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@RegisterActivity,
                            e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun logStateValidate(currentUser: FirebaseUser?) {
        if (authentication.currentUser != null) {
            Toast.makeText(
                this@RegisterActivity,
                "Successfully Logged In",
                Toast.LENGTH_LONG
            ).show()
            updateUI(currentUser)
            /*startActivity(
                Intent(
                this@RegisterActivity,
                HomepageActivity::class.java
            ))*/
            finish()
            Log.d(TAG, "Token has been found")
        } else {
            Log.w(TAG, "Token not found")
        }
    }

    private fun linkSetup() {
        val loginLink = Link("Login here!")
            .setTextColor(Color.parseColor("#004CE8"))
            .setHighlightAlpha(.4f)
            .setUnderlined(false)
            .setBold(true)
            .setOnClickListener {
                startActivity(Intent(
                    this@RegisterActivity,
                    LoginActivity::class.java
                ))
            }
        val bindingLink = binding.loginLink
        bindingLink.applyLinks(loginLink)
    }

    /*private fun userRegisterWithEmail(userName: String, userEmail: String, userPassword: String) {
        authentication.createUserWithEmailAndPassword(userEmail, userPassword)
            .addOnCompleteListener(this) { authenticationRes ->
                if (authenticationRes.isSuccessful) {
                    updateData(userName, userEmail, userPassword)
                    Toast.makeText(this@RegisterActivity, "Successfully Register Account", Toast.LENGTH_LONG).show()
                    val intent = Intent(
                        this@RegisterActivity,
                        LoginActivity::class.java
                    )
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        "${authenticationRes.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }*/

    /*private fun updateData(userName: String, userEmail: String, userPassword: String) {

        val authUser: FirebaseUser? = authentication.currentUser
        val userId = authUser?.uid

    }*/

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            startActivity(Intent(
                this@RegisterActivity,
                LoginActivity::class.java
            ))
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = authentication.currentUser
        logStateValidate(currentUser)
    }

    companion object {
        private const val TAG = "Register"
    }
}