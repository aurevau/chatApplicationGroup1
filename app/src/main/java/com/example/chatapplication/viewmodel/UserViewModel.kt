package com.example.chatapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.chatapplication.data.User
import com.example.chatapplication.repository.UserRepository


class UserViewModel: ViewModel() {



    private val dataManager = UserRepository()

    val user: LiveData<MutableList<User>> = dataManager.users
    val friends: LiveData<MutableList<User>> = dataManager.friends

    
    
    fun getCurrentUserId(): String? {
        return dataManager.getCurrentUserId()
    }
    fun addUser(fullName: String) {
        dataManager.addUser(fullName)
    }

    fun addFriend(currentUserId: String?, friend: User) {
        if (currentUserId != null) {
            dataManager.addFriend(currentUserId, friend)
        }
    }

    fun isFriend(currentUserId: String, otherUserId: String, callback: (Boolean) -> Unit) {
        dataManager.isFriend(currentUserId,otherUserId, callback)
    }

        fun removeFriend(currentUserId: String?, friendId: String?) {
            if (currentUserId != null) {
                if (friendId != null) {
                    dataManager.removeFriend(currentUserId, friendId)
                }
            }
    }

    fun getFriends(currentUserId: String) {
        dataManager.getFriends(currentUserId)
    }


    fun searchUsers(searchTerm: String) {
        dataManager.searchUsers(searchTerm)
    }

    fun updateCurrentUser(fullName: String, username: String) {
        val id = getCurrentUserId() ?: return
        dataManager.updateCurrentUser(fullName)
    }


    fun deleteCurrentUser() {
        val id = getCurrentUserId() ?: return
        dataManager.deleteCurrentUser()
    }


}