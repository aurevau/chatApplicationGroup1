package com.example.chatapplication.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapplication.databinding.ActivityChatBinding
import com.example.chatapplication.adapter.ChatRecyclerAdapter
import com.example.chatapplication.viewmodel.ChatViewModel

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = intent.getStringExtra("USER_ID")

        viewModel.getUserDetailsById(userId)

        //receive target user
        viewModel.targetUser.observe(this){ user->
            binding.tvHeader.text = user?.fullName
            binding.tvInitials.text = user?.fullName?.take(2)

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