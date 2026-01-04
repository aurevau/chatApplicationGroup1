package com.example.chatapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.chatapplication.data.User
import com.example.chatapplication.repository.UserRepository


class UserViewModel: ViewModel() {



    private val dataManager = UserRepository()

    val user: LiveData<MutableList<User>> = dataManager.users
    val friends: LiveData<MutableList<User>> = dataManager.friends
    val selection: LiveData<MutableList<User>> = dataManager.selection

    val recentSearchedUsers: LiveData<List<User>> = dataManager.recentSearchedUsers
    val searchResults: LiveData<List<User>> = dataManager.searchResults





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

    fun searchUsers(searchTerm: String) {
        dataManager.searchUsers(searchTerm)
    }

    fun getUserDetailsById(userId: String, callback: (User?) -> Unit) {
        dataManager.getUserDetailsById(userId, callback)
    }


    fun loadRecentSearches() {
        val currentUserId = getCurrentUserId() ?: return
        dataManager.loadRecentSearches(currentUserId)
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

    fun isSelected(currentUserId: String?, other: User) {
        dataManager.isSelected(currentUserId, other)
    }

    fun isNotSelected(currentUserId: String?, otherUserId: String?) {
        dataManager.isNotSelected(currentUserId, otherUserId)
    }

    fun getSelection(currentUserId: String, otherUserId: String) {
        dataManager.getSelection(currentUserId, otherUserId)
    }




    fun updateCurrentUser(fullName: String, username: String) {
        val id = getCurrentUserId() ?: return
        dataManager.updateCurrentUser(fullName)
    }



    fun deleteCurrentUser() {
        val id = getCurrentUserId() ?: return
        dataManager.deleteCurrentUser()
    }

    fun addRecentSearch(user: User) {
        dataManager.addRecentSearch(user)
    }

    fun clearRecentSearches() {
        dataManager.clearRecentSearches()
    }



}