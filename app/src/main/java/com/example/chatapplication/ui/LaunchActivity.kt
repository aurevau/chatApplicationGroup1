package com.example.chatapplication.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.chatapplication.databinding.ActivityLaunchBinding
import com.example.chatapplication.viewmodel.AuthViewModel
import com.example.chatapplication.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class LaunchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLaunchBinding
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLaunchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]







        Handler(Looper.getMainLooper()).postDelayed({

            if (authViewModel.isLoggeedIn()) {
                // User already logged in
                startActivity(Intent(this, DashboardActivity::class.java))
            } else {
                // User not logged in
                startActivity(Intent(this, WelcomeActivity::class.java))
            }

            finish()

        }, 1000) // 1 second delay
    }
}
