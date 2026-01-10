package com.example.chatapplication.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.chatapplication.R
import com.example.chatapplication.databinding.ActivityWelcomeBinding
import com.example.chatapplication.repository.UserRepository
import com.example.chatapplication.ui.DashboardActivity
import com.example.chatapplication.viewmodel.AuthViewModel
import com.google.android.gms.common.SignInButton
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class WelcomeActivity : AppCompatActivity() {

    private lateinit var credentialManager: CredentialManager
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var authViewModel: AuthViewModel
    private val userRepository = UserRepository()

    private lateinit var emailEditText: TextInputLayout
    private lateinit var passwordEditText: TextInputLayout

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        credentialManager = CredentialManager.create(this)

        emailEditText = binding.editTextEmail
        passwordEditText = binding.editTextPassword




        val emailFromIntent = intent.getStringExtra("EMAIL")
        val passwordFromIntent = intent.getStringExtra("PASSWORD")


        emailEditText.editText?.setText(emailFromIntent ?: "")
        passwordEditText.editText?.setText(passwordFromIntent ?: "")

        // Initiate Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]


        binding.buttonLogIn.setOnClickListener {
            val email = binding.editTextEmail.editText?.text.toString().trim()
            val password = binding.editTextPassword.editText?.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                binding.editTextEmail.editText?.error =
                    getString(R.string.edit_text_error_text_empty)
                binding.editTextPassword.editText?.error =
                    getString(R.string.edit_text_error_text_empty)
                return@setOnClickListener
            }

            if (binding.editTextEmail.editText?.text?.isNotEmpty() == true) {
                login()
            }
        }

        binding.buttonRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        binding.btnGoogleLogin.setSize(SignInButton.SIZE_WIDE)
        binding.btnGoogleLogin.setOnClickListener {
            loginWithGoogle()
        }

    }

    private fun loginWithGoogle() {
        lifecycleScope.launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(baseContext.getString(R.string.default_web_client_id))
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(this@WelcomeActivity, request)
                handleSignIn(result)
            } catch (exception: GetCredentialException) {
                handleFailure(exception)
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        if (result.credential is CustomCredential && result.credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)

            val idToken = googleIdTokenCredential.idToken
            authViewModel.loginWithGoogle(idToken, {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                    ?: return@loginWithGoogle
                val user = userRepository.getUserDetailsById(userId) {user ->

                    if (user?.fullName.isNullOrEmpty()) {
                        intent = Intent(this, ProfileActivity::class.java)
                        startActivity(intent)
                    } else {
                        intent = Intent(this, DashboardActivity::class.java)
                        startActivity(intent)
                    }
                }


            }, {
                Toast.makeText(this, getString(R.string.not_successful, it.message), Toast.LENGTH_SHORT).show()
            })
        }

    }

    private fun handleFailure(exception: GetCredentialException) {
        when (exception) {
            is GetCredentialCancellationException -> {
                Toast.makeText(this,
                    getString(R.string.not_successful, exception.message), Toast.LENGTH_SHORT).show()

            }
            is NoCredentialException -> {

                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.add_google_account))
                    .setMessage(getString(R.string.add_google_account_long_text))
                    .setPositiveButton(getString(R.string.yes_go_to_settings)) { dialog, _ ->
                        val intent = Intent(Settings.ACTION_SETTINGS)
                        startActivity(intent)
                        dialog.dismiss()
                    }
                    .setNegativeButton(getString(R.string.cancel_alert_btn_text)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
            else -> {
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()

            }
        }
    }

    fun login() {
        val email = binding.editTextEmail.editText?.text.toString()
        val password = binding.editTextPassword.editText?.text.toString()

        authViewModel.login(email, password, onSuccess = {
            clearFields()
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)

        }, onFailure = {
            Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
        })

    }

    fun clearFields() {
        binding.editTextEmail.editText?.text?.clear()
        binding.editTextPassword.editText?.text?.clear()
    }
}