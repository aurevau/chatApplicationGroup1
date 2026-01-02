package com.example.chatapplication.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chatapplication.data.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestore
import com.google.firebase.auth.auth


class UserRepository {
    private val db = Firebase.firestore

    // Livedata för users
    private val _users = MutableLiveData<MutableList<User>>()
    val users: LiveData<MutableList<User>> get() = _users

    private lateinit var currentUser: FirebaseUser

    init {
        listenToUsers()
    }

    fun listenToUsers() {
//       currentUser = Firebase.auth.currentUser ?: return

        db.collection("users").addSnapshotListener { snapshot, error ->
            if (snapshot != null)  {
                val tempList = mutableListOf<User>()

                for (doc in snapshot.documents) {
                    val user = doc.toObject(User::class.java)
                    if (user != null) {
                        tempList.add(user.copy(id = doc.id))
                    }

                }
                _users.value = tempList

            }
        }
    }

    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun addUser(name: String) {
        currentUser = Firebase.auth.currentUser ?: return

        val fields = mapOf(
            "name" to name
        )

        db.collection("users").document(currentUser.uid).set(fields)
            .addOnSuccessListener {
            Log.i("SOUT", "added user to database with id:  ${currentUser?.uid}")
        }.addOnFailureListener { exception ->
            Log.e("SOUT", "failed to add user to database, error: " + exception.message )
        }
    }



    fun updateCurrentUser(id: String, name: String) {
        val fields = mapOf(
            "name" to name
        )

        db.collection("users").document(id).update(fields).addOnSuccessListener { documentReference ->
            Log.i("SOUT", "updated user to database with id: $id")
        }.addOnFailureListener { exception ->
            Log.e("SOUT", "failed to update user to database, error: " + exception.message )
        }
    }

    fun deleteCurrentUser(id: String) {
        currentUser = Firebase.auth.currentUser ?: return
        db.collection("users").document(id).delete().addOnSuccessListener {
            Log.i("SOUT", "deleted user from database with id: $id")
        }.addOnFailureListener { exception ->
            Log.e("SOUT", "failed to delete user from database, error: " + exception.message)
        }
    }
}