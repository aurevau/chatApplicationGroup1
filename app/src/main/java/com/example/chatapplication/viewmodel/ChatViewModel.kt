package com.example.chatapplication.viewmodel

import android.net.Uri
import android.util.Log
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


    //get target user details
    fun getUserDetailsById(userID:String?){
        if(userID == null) return

        userRepository.getUserDetailsById(
            userId = userID ,
            { user->
               targetUser.value = user
             }
        )
    }

    fun sendImageMessage(roomId: String, imageUrl: String, text: String?){
        messageRepository.sendImageMessage(roomId, imageUrl, text)
    }

    fun uploadChatImage(
        imageUri: Uri,
        roomId: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        messageRepository.uploadChatImage(imageUri, roomId, onSuccess, onError)
    }

    val selectedImageUri = MutableLiveData<Uri?>()



    fun sendTextMessage(roomId: String, text: String) {
        messageRepository.sendTextMessage(roomId, text)
    }


    fun createGroupChat(roomId: String, userIds: List<String>, groupName: String, onSuccess: (String) -> Unit) {
        messageRepository.createGroupChat(roomId, userIds, groupName, onSuccess)
    }

    fun sendImageIfSelected(roomId: String, text: String? = null) {
        selectedImageUri.value?.let { uri ->
            uploadChatImage(uri, roomId,
                onSuccess = { imageUrl ->
                    // Skicka både bild + text i samma meddelande
                    messageRepository.sendImageMessage(roomId, imageUrl, text)
                    selectedImageUri.value = null
                },
                onError = { e ->
                    Log.e("ChatImage", "Failed to send image", e)
                }
            )
        }
    }



}