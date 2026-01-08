package com.example.chatapplication.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.chatapplication.R
import com.example.chatapplication.databinding.ActivityRegisterBinding
import com.example.chatapplication.popup.RegisterPopupFragment
import com.example.chatapplication.viewmodel.AuthViewModel

class RegisterActivity : AppCompatActivity() {

    private var imageUri: Uri? = null
    private lateinit var authViewModel: AuthViewModel

    private lateinit var binding: ActivityRegisterBinding

    // Result launcher for picking an image
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUri = it
                binding.ivProfilePicture.setImageURI(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Click on the camera icon to choose new profile picture
        binding.imgBtnAddPhoto.setOnClickListener {
            chooseImage()
        }

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]



        binding.backBtn.setOnClickListener {
            finish()
        }


        binding.btnRegister.setOnClickListener {
            val fullName = binding.etFullName.editText?.text.toString().trim()
            val email = binding.etEmail.editText?.text.toString().trim()
            val password = binding.etPassword.editText?.text.toString().trim()

            val fullNameLower = fullName.lowercase()

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                binding.etFullName.editText?.error =
                    getString(R.string.edit_text_error_text_empty)
                binding.etEmail.editText?.error =
                    getString(R.string.edit_text_error_text_empty)
                binding.etPassword.editText?.error =
                    getString(R.string.edit_text_error_text_empty)
                return@setOnClickListener
            }

            if (password.length < 6) {
                binding.etPassword.editText?.error = getString(R.string.password_length_error_text)
                Toast.makeText(
                    this,
                    getString(R.string.password_length_error_text),
                    Toast.LENGTH_SHORT
                )
                    .show()
                return@setOnClickListener
            }

            // Calling the new register function with imageUri and callbacks
            authViewModel.register(
                fullName,
                fullNameLower,
                email,
                password,
                imageUri,
                onSuccess = {

                    val intent = Intent(this, WelcomeActivity::class.java)
                    intent.putExtra("EMAIL", email)
                    intent.putExtra("PASSWORD", password)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    showPopup()

                    Handler(Looper.getMainLooper()).postDelayed({
                        startActivity(intent)
                    }, 2500)
                },
                onFailure = { errorMessage ->

                    Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    fun showPopup() {
        val fragment = RegisterPopupFragment()
        fragment.show(supportFragmentManager, "RegisterPopupFragment")
    }

    private fun chooseImage() {
        pickImageLauncher.launch("image/*")
    }
}
