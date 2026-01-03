package com.example.chatapplication.ui.chat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatapplication.data.User
import com.example.chatapplication.repository.MessageRepository
import com.example.chatapplication.repository.UserRepository

class ChatViewModel : ViewModel() {
    private val messageRepository = MessageRepository()

    private val userRepository = UserRepository()

    val myUserId = userRepository.getCurrentUserId()

    val users = userRepository.users

    val messages = messageRepository.messages

    val targetUser = MutableLiveData<User?>()

    fun start(roomId: String) = messageRepository.listenToChat(roomId)
    fun send(text: String) {
         messageRepository.sendMessage( targetUserId = targetUser.value?.id ?: "" , text = text)
    }

    //get target user details
    fun getUserDetailsById(userID:String?){
        if(userID == null) return

        messageRepository.getUserDetailsById(
            userId = userID ,
            { user->
               targetUser.value = user
             }
        )
    }

}
