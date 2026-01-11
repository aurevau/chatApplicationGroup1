package com.example.chatapplication.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatapplication.data.ChatRoom
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
    val chatRoomDetails: LiveData<ChatRoom> = messageRepository.chatRoomDetails


    //get target user details
    fun getUserDetailsById(userID: String?) {
        if (userID == null) return

        userRepository.getUserDetailsById(
            userId = userID,
            { user ->
                targetUser.value = user
            }
        )
    }

    fun sendImageMessage(
        roomId: String,
        imageUrl: String,
        text: String?,
        otherUserId: String? = null
    ) {
        messageRepository.sendImageMessage(roomId, imageUrl, text, otherUserId)
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


    fun sendTextMessage(roomId: String, text: String, otherUserId: String? = null) {
        messageRepository.sendTextMessage(roomId, text, otherUserId)
    }


    fun createGroupChat(
        roomId: String,
        userIds: List<String>,
        groupName: String,
        memberNames: List<String>,
        onSuccess: (String) -> Unit
    ) {
        messageRepository.createGroupChat(roomId, userIds, groupName, memberNames, onSuccess)
    }

    fun sendImageIfSelected(roomId: String, text: String? = null) {
        selectedImageUri.value?.let { uri ->
            uploadChatImage(
                uri, roomId,
                onSuccess = { imageUrl ->
                    // Skicka både bild + text i samma meddelande
                    messageRepository.sendImageMessage(roomId, imageUrl, text, null)
                    selectedImageUri.value = null
                },
                onError = { e ->
                    Log.e("ChatImage", "Failed to send image", e)
                }
            )
        }
    }

    fun deleteMessage(messageId: String, roomId: String, senderId: String) {
        messageRepository.deleteMessage(messageId, roomId, senderId)
    }

    fun getChatRoomDetailsById(roomId: String) {
        messageRepository.getChatRoomDetailsById(roomId)
    }

    fun buildGroupName(allUserNames: List<String?>, currentUserFullName: String): String {
        return allUserNames
            .filter { it?.trim()?.equals(currentUserFullName.trim(), ignoreCase = true) == false }
            .joinToString(", ") { it?.substringBefore(" ")?.trim() ?: "" }
    }

    fun buildSingleChatName(allUserNames: List<String?>, currentUserFullName: String): String {
        val otherNames = allUserNames
            .filterNotNull()
            .filter { it.isNotBlank() && it != currentUserFullName }
            .map { it.substringBefore(" ") } // valfritt: bara förnamn

        return otherNames.firstOrNull() ?: ""
    }


    fun updateChatName(roomId: String, newName: String) {
        messageRepository.updateChatName(roomId, newName)
    }


}