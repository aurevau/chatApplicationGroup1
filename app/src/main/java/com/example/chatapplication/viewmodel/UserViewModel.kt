package com.example.chatapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.chatapplication.data.User
import com.example.chatapplication.repository.ChatRepository
import com.example.chatapplication.repository.UserRepository

class UserViewModel: ViewModel() {



    private val dataManager = UserRepository()

    val user: LiveData<MutableList<User>> = dataManager.users


    fun getCurrentUserId(): String? {
        return dataManager.getCurrentUserId()
    }
    fun addUser(username: String, name: String) {
        dataManager.addUser(username, name)
    }

    fun updateCurrentUser(name: String, username: String) {
        val id = getCurrentUserId() ?: return
        dataManager.updateCurrentUser(id, username, name)
    }


    fun deleteCurrentUser() {
        val id = getCurrentUserId() ?: return
        dataManager.deleteCurrentUser(id)
    }


}