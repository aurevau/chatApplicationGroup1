package com.example.chatapplication.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.chatapplication.data.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage

class AuthViewModel : ViewModel() {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore
    private val storage = FirebaseStorage.getInstance()

    fun register(fullName: String, fullNameLower: String, email: String, password: String, imageUri: Uri?, onSuccess: () -> Unit, onFailure: (String) -> Unit) {

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    if (imageUri != null) {
                        uploadProfileImage(imageUri, userId,
                            onSuccess =  { downloadUrl ->
                            saveUserToFirestore(fullName, fullNameLower, email, userId, downloadUrl, onSuccess, onFailure)
                                 }, onError = {error ->
                                     onFailure(error)
                            }
                        )
                    } else {
                        saveUserToFirestore(fullName, fullNameLower, email, userId, null, onSuccess, onFailure)
                    }
                }
            }
    }

    private fun uploadProfileImage(imageUri: Uri, userId: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val ref = storage.reference.child("profile_images/$userId")
        ref.putFile(imageUri)
            .addOnSuccessListener {
                val ref = storage.reference.child("profile_images/$userId")

                ref.putFile(imageUri)
                    .addOnSuccessListener {
                        ref.downloadUrl
                            .addOnSuccessListener { uri ->
                                onSuccess(uri.toString())
                            }
                            .addOnFailureListener {
                                onError(it.message ?: "Failed to get image URL")
                            }
                    }
                    .addOnFailureListener {
                        onError(it.message ?: "Image upload failed")
                    }
            }
    }

    private fun saveUserToFirestore(fullName: String, fullNameLower: String, email: String, userId: String, profileImageUrl: String?, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        // Step 9: Uppdatera User-klassen och spara till Firestore
        // Using the User data class instead of a HashMap for cleaner code and type safety
        val user = User(
            id = userId,
            fullName = fullName,
            email = email,
            profileImageUrl = profileImageUrl,
            fullNameLower = fullNameLower
        )

        firestore.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to save user data")
            }
    }

    fun isLoggedIn() : Boolean = auth.currentUser != null

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
