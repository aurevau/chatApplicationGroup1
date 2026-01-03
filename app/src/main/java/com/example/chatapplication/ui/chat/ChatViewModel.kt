package com.example.chatapplication.ui.chat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatapplication.ChatRoom
import com.example.chatapplication.data.User
import com.example.chatapplication.repository.MessageRepository

class ChatViewModel : ViewModel() {
    private val repo = MessageRepository()
    val messages = repo.messages

    val _user = MutableLiveData<User?>()

    fun start(roomId: String) = repo.listenToChat(roomId)
    fun send(roomId: String, text: String) = repo.sendMessage(roomId, text)

    fun getUserDetailsById(userID:String?){
        if(userID == null) return

        repo.getUserDetailsById(
            userId = userID ,
            { user->
               _user.value = user
             }
        )
    }



}
