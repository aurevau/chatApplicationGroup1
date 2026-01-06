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
                        uploadProfileImage(imageUri, userId) { downloadUrl ->
                            saveUserToFirestore(fullName, fullNameLower, email, userId, downloadUrl, onSuccess, onFailure)
                        }
                    } else {
                        saveUserToFirestore(fullName, fullNameLower, email, userId, null, onSuccess, onFailure)
                    }
                } else {
                    onFailure(task.exception?.message ?: "Registration failed")
                }
            }
    }

    private fun uploadProfileImage(imageUri: Uri, userId: String, onSuccess: (String) -> Unit) {
        val ref = storage.reference.child("profile_images/$userId")
        ref.putFile(imageUri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
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

    fun isLoggeedIn() : Boolean = auth.currentUser != null

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
