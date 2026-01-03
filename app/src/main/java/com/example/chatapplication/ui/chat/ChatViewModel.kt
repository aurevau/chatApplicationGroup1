package com.example.chatapplication.ui.chat

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatapplication.data.User
import com.example.chatapplication.repository.MessageRepository
import com.example.chatapplication.repository.UserRepository

class ChatViewModel : ViewModel() {
    private val messageRepository = MessageRepository()

    private val userRepository = UserRepository()

    val users = userRepository.users

    val messages = messageRepository.messages

    val targetUser = MutableLiveData<User?>()

    fun start(roomId: String) = messageRepository.listenToChat(roomId)
    fun send(roomId: String, text: String) = messageRepository.sendMessage(roomId, text)

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

    fun getMessagesList(){
        messageRepository.getMessagesList(
            myUserId = userRepository.getCurrentUserId() ?: "",
            targetUserId = targetUser.value?.id ?: "",
            { messages ->
                Log.d("messages_debug : list_of_messages" , messages.toString())
            }
        )
    }



}
