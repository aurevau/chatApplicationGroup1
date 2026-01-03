package com.example.chatapplication.ui.chat

import androidx.lifecycle.ViewModel
import com.example.chatapplication.repository.MessageRepository

class MessageViewModel : ViewModel() {
    private val repo = MessageRepository()
    val messages = repo.messages

    fun start(roomId: String) = repo.listenToChat(roomId)
    fun send(roomId: String, text: String) = repo.sendMessage(roomId, text)
}
