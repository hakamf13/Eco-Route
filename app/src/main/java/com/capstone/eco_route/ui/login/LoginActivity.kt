package com.capstone.eco_route.ui.login

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.capstone.eco_route.R
import com.capstone.eco_route.databinding.ActivityLoginBinding
import com.capstone.eco_route.ui.homepage.HomepageActivity
import com.capstone.eco_route.ui.register.RegisterActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.klinker.android.link_builder.Link
import com.klinker.android.link_builder.applyLinks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var authentication: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        authentication = FirebaseAuth.getInstance()

        linkSetup()

        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_clients_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        /* Sign Button */
        binding.signInButtonGoogle.setOnClickListener {
            signInWithGoogle()
        }

        binding.loginButton.setOnClickListener {
            signInWithEmail()
        }

    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }

    private fun signInWithEmail() {
        val userEmail = binding.customEmail.text.toString()
        val userPassword = binding.customPassword.text.toString()

        if (userEmail.isNotEmpty() && userPassword.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    authentication.signInWithEmailAndPassword(userEmail, userPassword).await()
                    withContext(Dispatchers.Main) {
                        logStateValidate(currentUser = null)
                    }
                } catch (e: ApiException) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@LoginActivity,
                            e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun  logStateValidate(currentUser: FirebaseUser?) {
        if (authentication.currentUser != null) {
            Toast.makeText(
                this@LoginActivity,
                "Successfully Logged In",
                Toast.LENGTH_LONG
            ).show()
            updateUI(currentUser)
            finish()
            Log.d(TAG, "Token has been found")
        } else {
            Log.w(TAG, "Token not found")
        }
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val userAccount = task.getResult(ApiException::class.java)
                Log.d(TAG, "userAuthentication: " + userAccount.id)
                userAuthWithGoogle(userAccount.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "User Authentication has been failed", e)
            }
        }
    }

    private fun userAuthWithGoogle(idToken: String) {
        val userCredential = GoogleAuthProvider.getCredential(idToken, null)
        authentication.signInWithCredential(userCredential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Authentication Success", Toast.LENGTH_LONG).show()
                    Log.d(TAG, "Success")
                    val userCurrent = authentication.currentUser
                    updateUI(userCurrent)
                } else {
                    Toast.makeText(this, "Authentication Failed", Toast.LENGTH_LONG).show()
                    Log.w(TAG, "Failed",task.exception)
                    updateUI(null)
                }
            }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            startActivity(Intent(
                this@LoginActivity,
                HomepageActivity::class.java
            ))
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = authentication.currentUser
        logStateValidate(currentUser)
        updateUI(currentUser)
    }

    private fun linkSetup() {
        val loginLink = Link("Register here!")
            .setTextColor(Color.parseColor("#004CE8"))
            .setHighlightAlpha(.4f)
            .setUnderlined(false)
            .setBold(true)
            .setOnClickListener {
                startActivity(Intent(
                    this@LoginActivity,
                    RegisterActivity::class.java
                ))
            }
        val bindingLink = binding.loginLink
        bindingLink.applyLinks(loginLink)
    }

    companion object {
        private const val TAG = "Login"
    }

}