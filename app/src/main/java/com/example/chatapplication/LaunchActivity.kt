package com.example.chatapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapplication.databinding.ActivityLaunchBinding
import com.google.firebase.auth.FirebaseAuth

class LaunchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLaunchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLaunchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentUser = FirebaseAuth.getInstance().currentUser


        Handler(Looper.getMainLooper()).postDelayed({

            if (currentUser != null) {
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
