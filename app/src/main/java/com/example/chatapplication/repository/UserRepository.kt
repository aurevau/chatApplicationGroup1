package com.example.chatapplication.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chatapplication.data.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


class UserRepository {
    private val listeners = mutableListOf<ListenerRegistration>()

    private val db = Firebase.firestore

    // Livedata för users
    private val _users = MutableLiveData<MutableList<User>>()
    val users: LiveData<MutableList<User>> get() = _users


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

        val term = searchTerm
        val result = mutableListOf<User>()

        val queryName = allUsers()
            .orderBy("fullName")
            .startAt(term)
            .endAt(term + "\uf8ff")

        val listenerName = queryName.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("SOUT", "Error fetching username: ${error.message}")
                return@addSnapshotListener
            }
            val searchList =
                snapshot?.documents?.mapNotNull { it.toObject(User::class.java) } ?: emptyList()
            Log.d("SOUT", "Found ${searchList.size} users by username for term '$term'")
            updateResults(result, searchList)
        }
        listeners.add(listenerName)

        val lowerSearchTerm = term.lowercase()
        val queryLower = allUsers()
            .orderBy("fullNameLower")
            .startAt(lowerSearchTerm)
            .endAt(lowerSearchTerm + "\uf8ff")

        val listenerLower = queryLower.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("SOUT", "Error fetching username: ${error.message}")
                return@addSnapshotListener
            }
            val searchList =
                snapshot?.documents?.mapNotNull { it.toObject(User::class.java) } ?: emptyList()
            Log.d("SOUT", "Found ${searchList.size} users by fullNameLower for term '$lowerSearchTerm'")
            updateResults(result, searchList)
        }
        listeners.add(listenerLower)


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