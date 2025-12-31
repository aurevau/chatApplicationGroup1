package com.example.chatapplication.ui.chat

import androidx.lifecycle.ViewModel
import com.example.chatapplication.repository.ChatRepository

class ChatViewModel : ViewModel() {
    private val repo = ChatRepository()
    val messages = repo.messages

    fun start(roomId: String) = repo.listenToChat(roomId)
    fun send(roomId: String, text: String) = repo.sendMessage(roomId, text)
}
