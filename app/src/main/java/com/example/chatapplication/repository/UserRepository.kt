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

    private val db = Firebase.firestore

    // Livedata för users
    private val _users = MutableLiveData<MutableList<User>>()
    val users: LiveData<MutableList<User>> get() = _users

    private val _friends = MutableLiveData<MutableList<User>>()
    val friends: LiveData<MutableList<User>> get() = _friends

    private val _outgoingFriendRequests = MutableLiveData<List<User>>()
    val outgoingFriendRequests: LiveData<List<User>> get() = _outgoingFriendRequests

    private val _incomingFriendRequests = MutableLiveData<List<User>>()
    val incomingFriendRequests: LiveData<List<User>> get() = _incomingFriendRequests

    private val _selection = MutableLiveData<MutableList<User>?>()
    val selection: LiveData<MutableList<User>?> get() = _selection

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
        val selectedData = mapOf("fullName" to other.fullName)

        if (currentUserId != null) {
            db.collection("users")
                .document(currentUserId)
                .collection("isSelected")
                .document(other.id!!)
                .set(selectedData)
                .addOnSuccessListener {
                    Log.d("SOUT", "User is selected")
                    // Update livedata directly
                    val current = _selection.value ?: mutableListOf()
                    _selection.postValue((current + other).toMutableList())
                }
                .addOnFailureListener { exception ->
                    Log.e("SOUT", "Error selecting user", exception)
                }
        }
    }

    fun isNotSelected(currentUserId: String?, otherUserId: String?) {
        if (currentUserId != null && otherUserId != null) {
            db.collection("users")
                .document(currentUserId)
                .collection("isSelected")
                .document(otherUserId)
                .delete()
                .addOnSuccessListener {
                    Log.d("SOUT", "User not selected")

                    // Get updated list from firestore and post to livedata
                    getSelection(currentUserId)
                }
                .addOnFailureListener { exception ->
                    Log.e("SOUT", "Error unselecting user", exception)
                }
        }
    }


    fun getSelection(currentUserId: String) {
        db.collection("users")
            .document(currentUserId)
            .collection("isSelected")
            .get()
            .addOnSuccessListener { snapshots ->
                val selectionList = snapshots.documents.mapNotNull { document ->
                    val userId = document.id
                    val fullName = document.getString("fullName") ?: ""
                    User(id = userId, fullName = fullName)
                }
                _selection.value = selectionList.toMutableList()
            }
    }

    fun clearSelection(currentUserId: String) {
        db.collection("users")
            .document(currentUserId)
            .collection("isSelected")
            .get()
            .addOnSuccessListener { snapshots ->
                val batch = db.batch()
                snapshots.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit().addOnSuccessListener {
                    _selection.value = mutableListOf() // Update livedata directly
                }
            }
    }


    fun deleteFriendFromFirebase(user: User) {
        val currentUserId = getCurrentUserId()
        val otherUserId = user.id

        if (currentUserId != null && otherUserId != null) {
            val batch = db.batch()

            batch.delete(
                db.collection("users")
                    .document(currentUserId)
                    .collection("friends")
                    .document(otherUserId)
            )
            batch.delete(
                db.collection("users")
                    .document(otherUserId)
                    .collection("friends")
                    .document(currentUserId)
            )

            batch.delete(
                db.collection("users")
                    .document(currentUserId)
                    .collection("outgoingRequests")
                    .document(otherUserId)
            )
            batch.delete(
                db.collection("users")
                    .document(currentUserId)
                    .collection("friendRequests")
                    .document(otherUserId)
            )
            batch.delete(
                db.collection("users")
                    .document(otherUserId)
                    .collection("outgoingRequests")
                    .document(currentUserId)
            )
            batch.delete(
                db.collection("users")
                    .document(otherUserId)
                    .collection("friendRequests")
                    .document(currentUserId)
            )

            batch.commit()
                .addOnSuccessListener {
                    Log.d("Friend", "Deleted friendship and cleared all requests between $currentUserId and $otherUserId")
                }
                .addOnFailureListener { e ->
                    Log.e("Friend", "Failed to delete friendship and requests", e)
                }
        }
    }




    fun deleteRecentSearch(user: User) {
        val currentUserId = getCurrentUserId()
        if (currentUserId != null && user.id != null) {
            db.collection("users")
                .document(currentUserId)
                .collection("recentSearches")
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
        FirebaseAuth.getInstance().currentUser?.delete()
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

    fun loadFriendsRealtime(currentUserId: String) {
        db.collection("users")
            .document(currentUserId)
            .collection("friends")
            .addSnapshotListener { snapshots, _ ->
                val friendList = snapshots?.documents?.mapNotNull { doc ->
                    User(
                        id = doc.id,
                        fullName = doc.getString("fullName") ?: ""
                    )
                } ?: emptyList()
                _friends.value = friendList.toMutableList()
                Log.d("FRIENDS", friendList.map { it.fullName }.toString())

            }
    }

    fun sendFriendRequest(fromUserId: String, fromUserName: String, toUserId: String, toUserName: String) {
        val batch = db.batch()

        // Create request with the recipient
        val requestRef = db.collection("users")
            .document(toUserId)
            .collection("friendRequests")
            .document(fromUserId)

        val data = mapOf(
            "fromUserId" to fromUserId,
            "fromUserName" to fromUserName,
            "status" to "pending",
            "createdAt" to Timestamp.now()
        )
        batch.set(requestRef, data)

        val outgoingRef = db.collection("users")
            .document(fromUserId)
            .collection("outgoingRequests")
            .document(toUserId)

        val outgoingData = mapOf(
            "toUserId" to toUserId,
            "toUserName" to toUserName,
            "status" to "pending",
            "createdAt" to Timestamp.now()
        )
        batch.set(outgoingRef, outgoingData)

        batch.commit()
    }

    fun acceptFriendRequest(currentUserId: String, otherUserId: String, currentUserName: String, otherUserName: String) {
        val batch = db.batch()

        val currentUserFriendRef = db.collection("users")
            .document(currentUserId)
            .collection("friends")
            .document(otherUserId)

        val otherUserFriendRef = db.collection("users")
            .document(otherUserId)
            .collection("friends")
            .document(currentUserId)

        batch.set(currentUserFriendRef, mapOf(
            "userId" to otherUserId,
            "fullName" to otherUserName
        ))

        batch.set(otherUserFriendRef, mapOf(
            "userId" to currentUserId,
            "fullName" to currentUserName
        ))

        batch.delete(
            db.collection("users")
                .document(currentUserId)
                .collection("friendRequests")
                .document(otherUserId)
        )
        batch.delete(
            db.collection("users")
                .document(currentUserId)
                .collection("outgoingRequests")
                .document(otherUserId)
        )
        batch.delete(
            db.collection("users")
                .document(otherUserId)
                .collection("friendRequests")
                .document(currentUserId)
        )
        batch.delete(
            db.collection("users")
                .document(otherUserId)
                .collection("outgoingRequests")
                .document(currentUserId)
        )

        batch.commit()
            .addOnSuccessListener {
                loadFriendsRealtime(currentUserId)
                loadIncomingFriendRequests(currentUserId)
                loadOutgoingFriendRequests(currentUserId)
            }
            .addOnFailureListener { e ->
                Log.e("Friend", "Failed to accept friend request", e)
            }
    }



    fun loadIncomingFriendRequests(currentUserId: String) {
        db.collection("users")
            .document(currentUserId)
            .collection("friendRequests")
            .addSnapshotListener { snapshots, _ ->
                val requests = snapshots?.documents?.mapNotNull { doc ->
                    val id = doc.getString("fromUserId") ?: return@mapNotNull null
                    val name = doc.getString("fromUserName") ?: "Unknown"
                    User(id = id, fullName = name)
                } ?: emptyList()
                _incomingFriendRequests.value = requests.toMutableList()
            }
    }

    fun loadOutgoingFriendRequests(currentUserId: String) {
        db.collection("users")
            .document(currentUserId)
            .collection("outgoingRequests")
            .addSnapshotListener { snapshots, _ ->
                val requests = snapshots?.documents?.mapNotNull { doc ->
                    val id = doc.getString("toUserId") ?: return@mapNotNull null
                    val name = doc.getString("toUserName") ?: "Unknown"
                    User(id = id, fullName = name)
                } ?: emptyList()
                _outgoingFriendRequests.value = requests.toMutableList()
            }
    }





    fun declineFriendRequest(currentUserId: String, otherUserId: String) {
        val batch = db.batch()

        val incomingRef = db.collection("users")
            .document(currentUserId)
            .collection("friendRequests")
            .document(otherUserId)
        batch.delete(incomingRef)

        val outgoingRef = db.collection("users")
            .document(otherUserId)
            .collection("outgoingRequests")
            .document(currentUserId)
        batch.delete(outgoingRef)

        batch.commit().addOnSuccessListener {
            Log.d("FRIEND_REQUEST", "Declined friend request from $otherUserId")

            // Update LiveData explicitly if needed
            val updatedIncoming = _incomingFriendRequests.value?.filter { it.id != otherUserId }
            _incomingFriendRequests.postValue(updatedIncoming!!)

            val updatedOutgoing = _outgoingFriendRequests.value?.filter { it.id != currentUserId }
            _outgoingFriendRequests.postValue(updatedOutgoing!!)
        }.addOnFailureListener { e ->
            Log.e("FRIEND_REQUEST", "Failed to decline friend request", e)
        }
    }

    fun cancelOutgoingFriendRequest(currentUserId: String, otherUserId: String, onComplete: () -> Unit = {}) {
        val batch = db.batch()

        val incomingRef = db.collection("users")
            .document(otherUserId)
            .collection("friendRequests")
            .document(currentUserId)

        val outgoingRef = db.collection("users")
            .document(currentUserId)
            .collection("outgoingRequests")
            .document(otherUserId)

        batch.delete(incomingRef)
        batch.delete(outgoingRef)

        batch.commit().addOnSuccessListener {
            Log.d("FRIEND_REQUEST", "Cancelled outgoing request to $otherUserId")
            onComplete()
            loadOutgoingFriendRequests(currentUserId)

        }.addOnFailureListener { e ->
            Log.e("FRIEND_REQUEST", "Failed to cancel outgoing request", e)
        }
    }


}