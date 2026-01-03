package com.example.chatapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AllChatsViewModel : ViewModel() {
    private val _recentChats = MutableLiveData<List<ChatRoom>>()
    val recentChats: LiveData<List<ChatRoom>> = _recentChats

    init {
        // Initialize with empty list or fetch data
        _recentChats.value = emptyList()
    }
}
