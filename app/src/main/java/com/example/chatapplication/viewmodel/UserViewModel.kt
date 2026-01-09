package com.example.chatapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.chatapplication.data.User
import com.example.chatapplication.repository.UserRepository


class UserViewModel: ViewModel() {



    private val dataManager = UserRepository()

    val user: LiveData<MutableList<User>> = dataManager.users
    val friends: LiveData<MutableList<User>> = dataManager.friends


    val selection: LiveData<MutableList<User>?> = dataManager.selection

    val recentSearchedUsers: LiveData<List<User>> = dataManager.recentSearchedUsers
    val searchResults: LiveData<List<User>> = dataManager.searchResults

    val outgoingFriendRequest: LiveData<List<User>> = dataManager.outgoingFriendRequests
    val incomingFriendRequest: LiveData<List<User>> = dataManager.incomingFriendRequests








    fun getCurrentUserId(): String? {
        return dataManager.getCurrentUserId()
    }
    fun addUser(fullName: String) {
        dataManager.addUser(fullName)
    }

    fun addFriend(currentUserId: String?, friend: User) {
        if (currentUserId != null) {
            dataManager.addFriendToFirebase(currentUserId, friend)
        }
    }

    fun searchUsers(searchTerm: String) {
        dataManager.searchUsers(searchTerm)
    }

    fun getUserDetailsById(userId: String, callback: (User?) -> Unit) {
        dataManager.getUserDetailsById(userId, callback)
    }


    fun isFriend(currentUserId: String, otherUserId: String, callback: (Boolean) -> Unit) {
        dataManager.isFriend(currentUserId,otherUserId, callback)
    }

        fun removeFriend(currentUserId: String?, friend: User) {
            if (currentUserId != null) {
                    dataManager.deleteFriendFromFirebase(friend)

            }
    }

    fun getFriends(currentUserId: String) {
        dataManager.loadFriendsRealtime(currentUserId)
    }

    fun isSelected(currentUserId: String?, other: User) {
        dataManager.isSelected(currentUserId, other)
    }

    fun isNotSelected(currentUserId: String?, otherUserId: String?) {
        dataManager.isNotSelected(currentUserId, otherUserId)
    }

    fun getSelection(currentUserId: String) {
        dataManager.getSelection(currentUserId)
    }

    fun clearSelection() {
        val currentUserId = getCurrentUserId() ?: return
        dataManager.clearSelection(currentUserId)
    }




    fun updateCurrentUser(fullName: String, username: String) {
        val id = getCurrentUserId() ?: return
        dataManager.updateCurrentUser(fullName)
    }





    fun deleteCurrentUser() {
        val id = getCurrentUserId() ?: return
        dataManager.deleteCurrentUser()
    }

    fun addRecentSearchToFirebase(currentUserId: String, user: User) {
        dataManager.addRecentSearchToFirebase(currentUserId, user)
    }

    fun loadRecentSearches() {
        val currentUserId = dataManager.getCurrentUserId() ?: return
        dataManager.loadRecentSearchesRealtime(currentUserId)
    }

    fun clearRecentSearches() {
        dataManager.clearRecentSearches()
    }





    fun acceptFriendRequest(currentUserId: String, otherUserId: String, currentUserName: String, otherUserName: String) {
        dataManager.acceptFriendRequest(currentUserId, otherUserId, currentUserName, otherUserName)
    }

    fun sendFriendRequest( fromUserId: String, fromUserName: String, toUserId: String, toUserName: String) {
        dataManager.sendFriendRequest(fromUserId, fromUserName, toUserId, toUserName)
    }

    fun loadOutgoingFriendRequests(currentUserId: String) {
        dataManager.loadOutgoingFriendRequests(currentUserId)
    }



    fun cancelOutgoingFriendRequest(currentUserId: String, otherUserId: String, onComplete: () -> Unit = {}) {
        dataManager.cancelOutgoingFriendRequest(currentUserId, otherUserId)
    }

    fun loadIncomingFriendRequests(currentUserId: String) {
        dataManager.loadIncomingFriendRequests(currentUserId)
    }

    fun declineFriendRequest(currentUserId: String, otherUserId: String) {
        dataManager.declineFriendRequest(currentUserId, otherUserId)
    }






}