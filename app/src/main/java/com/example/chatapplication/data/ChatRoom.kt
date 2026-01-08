package com.example.chatapplication.data
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatRoom(
    val userName: String? = null,
    val chatRoomImageUrl: String? = null,
    val groupName: String? = null,
    val lastMessage: String? = null,
    val timestamp: String? = null,
    val roomId: String? = null,
    val isGroup: Boolean = false
) : Parcelable