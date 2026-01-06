package com.example.chatapplication.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.chatapplication.data.ChatRoom
import com.example.chatapplication.repository.MessageRepository

class AllChatsViewModel : ViewModel() {
    private val messageRepository = MessageRepository()

    val recentChats: LiveData<List<ChatRoom>> = messageRepository.recentChats

    init {
        messageRepository.getRecentChats()
    }

}