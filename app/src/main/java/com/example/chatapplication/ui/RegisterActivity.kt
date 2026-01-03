package com.example.chatapplication.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.chatapplication.databinding.ActivityRegisterBinding
import com.example.chatapplication.viewmodel.AuthViewModel
class RegisterActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]


        // Back-knapp – går tillbaka till föregående aktivitet
        binding.backBtn.setOnClickListener {
            finish()
        }

        // Register-knapp
        binding.btnRegister.setOnClickListener {
            val fullName = binding.etFullName.editText?.text.toString().trim()
            val email = binding.etEmail.editText?.text.toString().trim()
            val password = binding.etPassword.editText?.text.toString().trim()
            val fullNameLower = fullName.lowercase()
            // Enkel validering
            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                binding.etFullName.editText?.error = "Field cannot be empty"
                binding.etEmail.editText?.error = "Field cannot be empty"
                Toast.makeText(this, "Fyll i alla fält", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                binding.etPassword.editText?.error = "Password needs to be at least 6 characters"
                Toast.makeText(this, "Password needs to be at least 6 characters", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            authViewModel.register(fullName, fullNameLower,email, password)
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.putExtra("EMAIL", email)
            intent.putExtra("PASSWORD", password)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

//            finish()  // Gå tillbaka till föregående skärm
        }
    }
}