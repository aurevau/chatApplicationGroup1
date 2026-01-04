package com.example.chatapplication.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.chatapplication.databinding.ActivityWelcomeBinding
import com.example.chatapplication.repository.UserRepository
import com.example.chatapplication.ui.DashboardActivity
import com.example.chatapplication.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WelcomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var authViewModel: AuthViewModel

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initiate Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]


        binding.buttonLogIn.setOnClickListener {
            login()
        }

        binding.buttonRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

    }

    fun login() {
        val email = binding.editTextEmail.editText?.text.toString()
        val password = binding.editTextPassword.editText?.text.toString()

        authViewModel.login(email, password, onSuccess = {
            clearFields()
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }, onFailure =  {
            Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
        })

    }

    fun clearFields() {
        binding.editTextEmail.editText?.text?.clear()
        binding.editTextPassword.editText?.text?.clear()
    }
}