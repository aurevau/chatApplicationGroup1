package com.example.chatapplication.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chatapplication.data.ChatRoom
import com.example.chatapplication.data.User
import com.example.chatapplication.util.DateUtils
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class UserRepository {
    private val listeners = mutableListOf<ListenerRegistration>()
    private var friendsListener: ListenerRegistration? = null

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

    private val friendList = mutableListOf<User>()


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


    fun allUsers(): CollectionReference = db.collection("users")

    fun getCurrentUserId(): String? = FirebaseAuth.getInstance().currentUser?.uid

    fun currentUserDetails(): DocumentReference {
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


    fun clearRecentSearches() {
        recentList.clear()
        _recentSearchedUsers.value = recentList
    }

    fun addFriendToFirebase(currentUserId: String, user: User) {
        val recentRef = db.collection("users")
            .document(currentUserId)
            .collection("friends")
            .document(user.id!!)

        val data = mapOf(
            "friendName" to user.fullName
        )

        recentRef.set(data)
            .addOnSuccessListener {
                Log.d("Friend", "Saved as friend ${user.fullName}")
            }.addOnFailureListener { e -> Log.e("Friend", "Failed to save friend", e) }

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
        val selectedData = mapOf(
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


    fun deleteFriendFromFirebase(user: User) {
        val currentUserId = getCurrentUserId()
        if (currentUserId != null && user.id != null) {
            db.collection("users")
                .document(currentUserId)
                .collection("friends")
                .document(user.id)
                .delete()
                .addOnSuccessListener {
                    Log.d("Friend", "Deleted friend ${user.fullName}")
                }
                .addOnFailureListener { e ->
                    Log.e("Friend", "Failed to delete friend ${user.fullName}", e)
                }
        }
    }



    fun deleteRecentSearch(user: User) {
        val currentUserId = getCurrentUserId()
        if (currentUserId != null && user.id != null) {
            db.collection("users")
                .document(currentUserId)
                .collection("recentSearches") // ⚠️ korrekt collection
                .document(user.id)
                .delete()
                .addOnSuccessListener {
                    Log.d("RECENT_SEARCH", "Deleted ${user.fullName}")
                }
                .addOnFailureListener { e ->
                    Log.e("RECENT_SEARCH", "Failed to delete ${user.fullName}", e)
                }
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
                Log.e("SOUT", "failed to add user to database, error: " + exception.message)
            }
    }


    fun updateCurrentUser(fullName: String) {
        val uid = getCurrentUserId() ?: return
        val fields = mapOf(
            "fullName" to fullName
        )

        db.collection("users").document(uid).update(fields)
            .addOnSuccessListener { documentReference ->
                Log.i("SOUT", "updated user to database with id: ${uid}")
            }.addOnFailureListener { exception ->
            Log.e("SOUT", "failed to update user to database, error: " + exception.message)
        }
    }

    fun deleteCurrentUser() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        db.collection("users").document(user.uid).delete().addOnSuccessListener {
            user.delete().addOnSuccessListener {
                Log.i("SOUT", "deleted user from database and auth with id:${user.uid}")
            }.addOnFailureListener {
                Log.e("SOUT", "Auth delete failed: ${it.message}")
            }
        }
            .addOnFailureListener { exception ->
                Log.e("SOUT", "failed to delete user from database, error: " + exception.message)
            }
    }

    fun loadFriendsRealtime(currentUserId: String) {
        db.collection("users")
            .document(currentUserId)
            .collection("friends")
            .addSnapshotListener { snapshots, _ ->
                val friendList = snapshots?.documents?.mapNotNull { doc ->
                    User(
                        id = doc.id,
                        fullName = doc.getString("friendName") ?: ""
                    )
                } ?: emptyList()
                _friends.value = friendList.toMutableList()
                Log.d("FRIENDS", friendList.map { it.fullName }.toString())

            }
    }

    fun loadRecentSearchesRealtime(currentUserId: String) {
        db.collection("users")
            .document(currentUserId)
            .collection("recentSearches")
            .addSnapshotListener { snapshots, _ ->
                val recent = snapshots?.documents?.mapNotNull { doc ->
                    User(
                        id = doc.id,
                        fullName = doc.getString("fullName") ?: ""
                    )
                } ?: emptyList()
                _recentSearchedUsers.value = recent
            }
    }
}