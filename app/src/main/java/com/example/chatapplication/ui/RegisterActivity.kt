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
                Toast.makeText(this, "Fyll i alla fält", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Lösenordet måste vara minst 6 tecken", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            authViewModel.register(fullName, fullNameLower,email, password)
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.putExtra("EMAIL", email)
            intent.putExtra("PASSWORD", password)
            startActivity(intent)
//            finish()  // Gå tillbaka till föregående skärm
        }
    }
}