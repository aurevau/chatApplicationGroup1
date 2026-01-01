package com.example.chatapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.chatapplication.data.User
import com.example.chatapplication.repository.ChatRepository
import com.example.chatapplication.repository.UserRepository

class UserViewModel: ViewModel() {

    private val dataManager = UserRepository()

    val user: LiveData<MutableList<User>> = dataManager.users


    fun addUser(name: String, username: String) {
        dataManager.addUser(name, username)
    }

    fun updateUser(id: String, name: String, username: String) {
        dataManager.updateUser(id, username, name)
    }


    fun deleteUser(id: String) {
        dataManager.deleteUser(id)
    }

    fun getUser(id: String): User? {
        return dataManager.getUser(id)
    }
}