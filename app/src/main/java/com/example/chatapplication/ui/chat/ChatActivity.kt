package com.example.chatapplication.ui.chat

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chatapplication.R
import com.example.chatapplication.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val vm: ChatViewModel by viewModels()
    private val roomId = "global_room"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = MessageAdapter()
        binding.recyclerMessages.adapter = adapter

        vm.start(roomId)

        vm.messages.observe(this) {
            adapter.submitList(it)
            binding.recyclerMessages.scrollToPosition(it.size - 1)
        }

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString()
            if (text.isNotBlank()) {
                vm.send(roomId, text)
                binding.etMessage.text.clear()
            }
        }
    }
}