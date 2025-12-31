package com.example.chatapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapplication.databinding.ActivityWelcomeBinding
import com.example.chatapplication.ui.DashboardActivity

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /** A starting "log in"-function to make tests easier in the beginning
            * Opens Dashboard Activity and no text in edit texts needed
         */
        binding.buttonLogIn.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }

    }
}