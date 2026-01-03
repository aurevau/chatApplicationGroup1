package com.example.chatapplication.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapplication.R
import com.example.chatapplication.databinding.ActivityChatBinding
import com.example.chatapplication.adapter.ChatRecyclerAdapter
import com.example.chatapplication.viewmodel.AuthViewModel
import com.example.chatapplication.viewmodel.ChatViewModel

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var spinner: AppCompatSpinner

    private lateinit var authViewModel: AuthViewModel

    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]


        spinner = binding.menuSpinner
        val menuCategories = resources.getStringArray(R.array.menu_spinner)

        val spinnerAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, menuCategories)
        spinner.adapter = spinnerAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) return
                if (menuCategories[position] == "Logout") {
                    authViewModel.logOut()

                    // Starta WelcomeActivity med CLEAR_TASK
                    val intent = Intent(this@ChatActivity, WelcomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)


                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val userId = intent.getStringExtra("USER_ID")

        viewModel.getUserDetailsById(userId)

        //receive target user
        viewModel.targetUser.observe(this){ user->
            binding.tvHeader.text = user?.fullName
            binding.tvInitials.text = user?.initials

            // Calculate roomId from the two user IDs
            val myUserId = viewModel.myUserId ?: ""
            val targetUserId = user?.id ?: ""
            val sortedIds = listOf(myUserId, targetUserId).sorted()
            val roomId = "${sortedIds[0]}_${sortedIds[1]}"
            viewModel.start(roomId)
        }

        val adapter = ChatRecyclerAdapter()
        binding.recyclerMessages.adapter = adapter
        binding.recyclerMessages.layoutManager = LinearLayoutManager(this).apply {
            reverseLayout = false
            stackFromEnd = false
        }
        viewModel.messages.observe(this) { messageList ->
            adapter.submitList(messageList) {
                // Scroll after the list is submitted and laid out
                if (messageList.isNotEmpty()) {
                    binding.recyclerMessages.post {
                        binding.recyclerMessages.scrollToPosition(messageList.size - 1)
                    }
                }
            }
        }

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString()
            if (text.isNotBlank()) {
                viewModel.send(text)
                binding.etMessage.text.clear()
            }
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}