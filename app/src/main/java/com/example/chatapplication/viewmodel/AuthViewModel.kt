package com.example.chatapplication.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class AuthViewModel : ViewModel() {

    private val auth = Firebase.auth

    private val firestore = Firebase.firestore


    fun register(fullName: String, email: String, password: String) {

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    // Förbered extra data som ska sparas i Firestore
                    val userData = HashMap<String, Any>()
                    userData["fullName"] = fullName
                    userData["email"] = email

                    // Spara till Firestore
                    firestore.collection("users").document(userId)
                        .set(userData)


                    /**
                     * Kommenterade ut alla TOAST för att det egentligen inte ska ligga här i viewmodel,
                     * men vet inte hur jag ska få till det i aktiviteten.
                     */
//                        .addOnSuccessListener {
//                            Toast.makeText(this, "Registrering lyckades!", Toast.LENGTH_LONG)
//                                .show()
//                        }
//                        .addOnFailureListener { e ->
//                            Toast.makeText(
//                                this,
//                                "Fel vid sparande av data: ${e.message}",
//                                Toast.LENGTH_LONG
//                            ).show()
//                        }
//
//                } else {
//                    Toast.makeText(
//                        this,
//                        "Registrering misslyckades: ${task.exception?.message}",
//                        Toast.LENGTH_LONG
//                    ).show()
                }
            }
    }

    fun isLoggeedIn() : Boolean = auth.currentUser != null

    fun logOut() {
        FirebaseAuth.getInstance().signOut()
    }


    fun login(email: String, password: String, onSuccess: ()-> Unit, onFailure: (Exception)-> Unit) {

        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener {
            onFailure(it)
        }
    }
}