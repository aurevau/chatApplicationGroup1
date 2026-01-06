package com.example.chatapplication.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class AuthViewModel : ViewModel() {

    private val auth = Firebase.auth

    private val firestore = Firebase.firestore

    fun register(
        fullName: String,
        fullNameLower: String,
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    onResult(false, task.exception?.message)
                    return@addOnCompleteListener
                }

                val userId = auth.currentUser?.uid ?: return@addOnCompleteListener onResult(
                    false,
                    "User ID missing"
                )

                // Förbered extra data som ska sparas i Firestore
                val userData = HashMap<String, Any>()
                userData["fullName"] = fullName
                userData["fullNameLower"] = fullNameLower
                userData["email"] = email

                // Spara till Firestore
                firestore.collection("users").document(userId)
                    .set(userData)
                    .addOnSuccessListener {
                        onResult(true, null)
                    }
                    .addOnFailureListener { e ->
                        onResult(false, e.message)
                    }
            }
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun logOut() {
        FirebaseAuth.getInstance().signOut()
    }

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener {
            onFailure(it)
        }
    }
}