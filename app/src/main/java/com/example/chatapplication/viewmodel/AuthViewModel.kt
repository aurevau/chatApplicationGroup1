package com.example.chatapplication.viewmodel

import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.auth

class AuthViewModel : ViewModel() {

    val auth = Firebase.auth


    fun register(email: String, password: String, callback: (Task<AuthResult>)-> Unit) {

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { resultTask ->
                callback(resultTask)
            }
    }


    fun login(email: String, password: String, onSuccess: ()-> Unit, onFailure: (Exception)-> Unit) {

        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener {
            onFailure(it)
        }
    }
}