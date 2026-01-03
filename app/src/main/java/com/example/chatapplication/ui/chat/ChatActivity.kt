package com.example.chatapplication.ui.chat

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapplication.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels()
    private val roomId = "global_room"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val userId = intent.getStringExtra("USER_ID")

        viewModel.getUserDetailsById(userId)

        viewModel.targetUser.observe(this){ user->
            binding.userFullName.text = user?.fullName

            viewModel.getMessagesList()
        }

        Log.d("chat_activity" , userId.toString())

        val adapter = ChatAdapter()
        binding.recyclerMessages.adapter = adapter

        viewModel.start(roomId)

        viewModel.messages.observe(this) {
            adapter.submitList(it)
            binding.recyclerMessages.scrollToPosition(it.size - 1)
        }

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString()
            if (text.isNotBlank()) {
                viewModel.send(roomId, text)
                binding.etMessage.text.clear()
            }
        }
    }
}