package com.example.chatapplication.data

data class Message (
     val id:String = "",
     val senderId: String = "",
     val text: String = "",
     val timestamp: Long = System.currentTimeMillis()
)