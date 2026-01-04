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
import java.util.zip.ZipFile

class UserRepository {
    private val listeners = mutableListOf<ListenerRegistration>()

    private val db = Firebase.firestore

    // Livedata för users
    private val _users = MutableLiveData<MutableList<User>>()
    val users: LiveData<MutableList<User>> get() = _users

    private val _friends = MutableLiveData<MutableList<User>>()
    val friends: LiveData<MutableList<User>> get() = _friends

    


    init {
        listenToUsers()
    }

    fun listenToUsers() {
        db.collection("users").addSnapshotListener { snapshot, error ->
            if(error != null) {
                Log.e("FIRESTORE_ERROR", "Snapshot error: ${error.message}")
                return@addSnapshotListener
            }

            if (snapshot != null)  {
                val tempList = mutableListOf<User>()

                val myId = getCurrentUserId()
                for (doc in snapshot.documents) {
                    Log.d("FIRESTORE_DEBUG", "Doc data: ${doc.data}")  // <--- Kolla vad Firestore faktiskt returnerar
                    val user = doc.toObject(User::class.java)
                    if (user != null) {
                        tempList.add(user.copy(id = doc.id))
                    }
                }
                _users.value = tempList

            }
        }
    }

    fun searchUsers(searchTerm: String) {
        listeners.forEach { it.remove() }
        listeners.clear()

        val term = searchTerm.lowercase()
        val results = mutableListOf<User>()

        val query = allUsers()
            .orderBy("fullNameLower")
            .startAt(term)
            .endAt(term + "\uf8ff")


        val listenerQuery = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(User::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            updateResults(results, list)
        }
        listeners.add(listenerQuery)
    }

    fun searchUsersLocally(searchTerm: String) {
        val all = _users.value ?: return
        val term = searchTerm.trim().lowercase()

        val filtered = all.filter { user ->
            user.fullName.lowercase().contains(term)
        }

        _users.value = filtered.toMutableList()
    }



    private fun updateResults(results: MutableList<User>, newList: List<User>) {
        results.addAll(newList)
        _users.value = results.distinctBy { it.id } as MutableList<User>?
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

    fun resetToAllUsers(){
        listeners.forEach { it.remove() }
        listeners.clear()
        listenToUsers()
    }
}