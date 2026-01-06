package com.example.chatapplication.data

data class ChatRoom(
    val userName: String? = null,
    val groupName: String? = null,
    val lastMessage: String? = null,
    val timestamp: String? = null,
    val roomId: String? = null,
    val isGroup: Boolean = false
)