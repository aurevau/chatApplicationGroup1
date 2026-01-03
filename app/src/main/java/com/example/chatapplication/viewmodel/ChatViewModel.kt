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
    fun send(text: String) {
         messageRepository.sendMessage( targetUserId = targetUser.value?.id ?: "" , text = text)
    }

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

    fun sendImageMessage(targetUserId: String, imageUrl: String){
        messageRepository.sendImageMessage(targetUserId, imageUrl)
    }

    fun uploadChatImage(
        imageUri: Uri,
        roomId: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        messageRepository.uploadChatImage(imageUri, roomId, onSuccess, onError)
    }

    fun uploadAndSendImage(
        imageUri: Uri,
        roomId: String,
        targetUserId: String
    ) {
        messageRepository.uploadChatImage(
            imageUri = imageUri,
            roomId = roomId,
            onSuccess = { downloadUrl ->
                messageRepository.sendImageMessage(targetUserId, downloadUrl)
            },
            onError = {
                Log.e("ChatImage", "Upload failed", it)
            }
        )
    }

    val selectedImageUri = MutableLiveData<Uri?>()

    fun sendImageMessageIfExists(roomId: String) {
        selectedImageUri.value?.let { uri ->
            uploadChatImage(uri, roomId,
                onSuccess = { imageUrl ->
                    val targetUserId = targetUser.value?.id ?: return@uploadChatImage
                    sendImageMessage(targetUserId, imageUrl)
                    selectedImageUri.value = null // nollställ efter skick
                },
                onError = { e ->
                    // visa toast eller logga
                }
            )
        }
    }


}