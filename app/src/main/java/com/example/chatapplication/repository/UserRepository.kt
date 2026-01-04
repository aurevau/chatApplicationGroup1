package com.example.chatapplication.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chatapplication.data.User
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class UserRepository {
    private val listeners = mutableListOf<ListenerRegistration>()

    private val db = Firebase.firestore

    // Livedata för users
    private val _users = MutableLiveData<MutableList<User>>()
    val users: LiveData<MutableList<User>> get() = _users

    private val _friends = MutableLiveData<MutableList<User>>()
    val friends: LiveData<MutableList<User>> get() = _friends

    private val _selection = MutableLiveData<MutableList<User>>()
    val selection: LiveData<MutableList<User>> get() = _selection

    private val _recentSearchedUsers = MutableLiveData<List<User>>()
    val recentSearchedUsers: LiveData<List<User>> get() = _recentSearchedUsers

    private val _searchResults = MutableLiveData<List<User>>()
    val searchResults: LiveData<List<User>> get() = _searchResults

    private val recentList = mutableListOf<User>()




    fun searchUsers(searchTerm: String) {
        val term = searchTerm.lowercase()
        if (term.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        allUsers()
            .orderBy("fullNameLower")
            .startAt(term)
            .endAt(term + "\uf8ff")
            .get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.copy(id = doc.id)
                }
                _searchResults.value = users
            }
    }


    fun allUsers() : CollectionReference =db.collection("users")

    fun getCurrentUserId(): String? = FirebaseAuth.getInstance().currentUser?.uid

    fun currentUserDetails() : DocumentReference {
        val uid = getCurrentUserId() ?: throw Exception("User not logged in")
        return FirebaseFirestore.getInstance().collection("users").document(uid)
    }

    fun isFriend(currentUserId: String, otherUserId: String, callback: (Boolean) -> Unit) {
        db.collection("users")
            .document(currentUserId)
            .collection("friends")
            .document(otherUserId)
            .get()
            .addOnSuccessListener { document ->
                callback(document.exists()) // true om vän finns
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    //To get the target user's details
    fun getUserDetailsById(userId: String, callback: (User?) -> Unit) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    callback(user?.copy(id = document.id))
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                callback(null)
            }
    }

    fun addRecentSearch(user: User){
        val currentUserId = getCurrentUserId() ?: return

        recentList.removeAll { it.id == user.id }
        recentList.add(0, user)
        if(recentList.size > 10) recentList.removeLast()
        _recentSearchedUsers.value = recentList

        addRecentSearchToFirebase(currentUserId, user)
    }

    fun clearRecentSearches() {
        recentList.clear()
        _recentSearchedUsers.value = recentList
    }

    fun loadRecentSearches(currentUserId: String) {
        db.collection("users")
            .document(currentUserId)
            .collection("recentSearches")
            .orderBy("searchedAt", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { snapshots ->
                val recent = snapshots.documents.mapNotNull { doc ->
                    User(
                        id = doc.id,
                        fullName = doc.getString("fullName") ?: ""
                    )
                }
                recentList.addAll(recent)
                _recentSearchedUsers.value = recentList
            }
    }

    fun addRecentSearchToFirebase(currentUserId: String, user: User) {
        val recentRef = db.collection("users")
            .document(currentUserId)
            .collection("recentSearches")
            .document(user.id!!)

        val data = mapOf(
            "fullName" to user.fullName,
            "searchedAt" to Timestamp.now()
        )

        recentRef.set(data)
            .addOnSuccessListener {
                Log.d("RECENT_SEARCH", "Saved ${user.fullName}")
            }.addOnFailureListener { e -> Log.e("RECENT_SEARCH", "Failed to save", e) }
        _recentSearchedUsers.value = recentList

    }

    fun isSelected(currentUserId: String?, other: User) {
        val selectedData = mapOf (
            "fullName" to other.fullName,

        )

        if (currentUserId != null) {
            db.collection("users")
                .document(currentUserId)
                .collection("isSelected")
                .document(other.id!!)
                .set(selectedData)
                .addOnSuccessListener {
                    Log.d("SOUT", "User is selected")
                }
                .addOnFailureListener { exception ->
                    Log.e("SOUT", "Error selecting user", exception)
                }
        }
    }

    fun isNotSelected(currentUserId: String?, otherUserId: String?) {
        if (currentUserId != null) {
            if (otherUserId != null) {
                db.collection("users")
                    .document(currentUserId)
                    .collection("isSelected")
                    .document(otherUserId)
                    .delete()
                    .addOnSuccessListener {
                        Log.d("SOUT", "User not selected")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("SOUT", "Error unselecting user", exception)

                    }
            }
        }
    }

    fun getSelection(currentUserId: String, otherUserId: String) {
        db.collection("users")
            .document(currentUserId)
            .collection("isSelected")
            .get()
            .addOnSuccessListener { snapshots ->
                val selectionList = snapshots.documents.mapNotNull { document ->
                    val userId = document.id
                    val fullName = document.getString("fullName") ?: ""
                    User(
                        id = userId,
                        fullName = fullName,
                    )
                }
                _selection.value = selectionList as MutableList<User>?
            }
    }

    fun addFriend(currentUserId: String, friend: User) {
        val friendData = mapOf(
            "friendId" to friend.id,
            "friendName" to friend.fullName,
            "addedAt" to Timestamp.now()
        )

        db.collection("users").document(currentUserId)
            .collection("friends").document(friend.id!!)
            .set(friendData)
            .addOnSuccessListener {
                getFriends(currentUserId)
                Log.d("SOUT", "Friend added successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("SOUT", "Error adding friend", exception)

            }

    }

    fun removeFriend(currentUserId: String, friendId: String) {
        db.collection("users")
            .document(currentUserId)
            .collection("friends")
            .document(friendId)
            .delete()
            .addOnSuccessListener { Log.d("SOUT", "Friend removed")
                getFriends(currentUserId)}
            .addOnFailureListener { exception -> Log.e("SOUT", "Error removing friend", exception) }

    }

    fun getFriends(currentUserId: String){
        db.collection("users")
            .document(currentUserId)
            .collection("friends")
            .get()
            .addOnSuccessListener { snapshots ->
                val friendList = snapshots.documents.mapNotNull { document ->
                    User(
                        id = document.getString("friendId"),
                        fullName = document.getString("friendName") ?: ""
                    )
                }
                _friends.value = friendList as MutableList<User>?
            }
    }

    fun addUser(fullName: String) {
        val uid = getCurrentUserId() ?: return

        val fields = mapOf(
            "fullName" to fullName
        )

        db.collection("users").document(uid).set(fields)
            .addOnSuccessListener {
            Log.i("SOUT", "added user to database with id:  ${uid}")
        }.addOnFailureListener { exception ->
            Log.e("SOUT", "failed to add user to database, error: " + exception.message )
        }
    }



    fun updateCurrentUser(fullName: String) {
        val uid = getCurrentUserId() ?: return
        val fields = mapOf(
            "fullName" to fullName
        )

        db.collection("users").document(uid).update(fields).addOnSuccessListener { documentReference ->
            Log.i("SOUT", "updated user to database with id: ${uid}")
        }.addOnFailureListener { exception ->
            Log.e("SOUT", "failed to update user to database, error: " + exception.message )
        }
    }

    fun deleteCurrentUser() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        db.collection("users").document(user.uid).delete().addOnSuccessListener {
            user.delete().addOnSuccessListener {
                Log.i("SOUT", "deleted user from database and auth with id:${user.uid}")
            }.addOnFailureListener {
                Log.e("SOUT", "Auth delete failed: ${it.message}" )
            }
        }
            .addOnFailureListener { exception ->
            Log.e("SOUT", "failed to delete user from database, error: " + exception.message)
        }
    }
}