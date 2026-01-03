package com.example.chatapplication.data

data class Message (
     val id:String = "",
     val senderId: String = "",
     val receiverId: String = "",
     val text: String = "",
     //roomId is a unique key for the conversations to make the get messages queries easier
     val roomId: String = "",
     val timestamp: Long = System.currentTimeMillis()
)


