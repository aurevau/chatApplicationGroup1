package com.example.chatapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatapplication.data.ChatRoom

class AllChatsViewModel : ViewModel() {
    private val _recentChats = MutableLiveData<List<ChatRoom>>()
    val recentChats: LiveData<List<ChatRoom>> = _recentChats

    init {
        // Initialize with empty list or fetch data
        _recentChats.value = emptyList()
    }
}