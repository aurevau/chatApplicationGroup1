package com.example.chatapplication.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {


    // skapa Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore


    // deklarerar UI komponenterna
    private lateinit var etUsername: EditText
    private lateinit var etFullName: EditText
    private lateinit var etDateOfBirth: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnBack: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initiera Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Hitta views från layouten (använd exakta ID:n från din XML!)
        etUsername = findViewById(R.id.Username_et)          // byt mot ditt ID
        etFullName = findViewById(R.id.FullName_et)
        etDateOfBirth = findViewById(R.id.Date_et)
        etEmail = findViewById(R.id.Email_et)
        etPassword = findViewById(R.id.Password_et)
        btnRegister = findViewById(R.id.btn_Register)
        btnBack = findViewById(R.id.back_btn)                // eller den övre Back-texten


        // Back-knapp – går tillbaka till föregående aktivitet
        btnBack.setOnClickListener {
            finish()  // eller startActivity(Intent(this, MainActivity::class.java))
        }

        // Register-knapp
        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val fullName = etFullName.text.toString().trim()
            val dateOfBirth = etDateOfBirth.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Enkel validering
            if (username.isEmpty() || fullName.isEmpty() || dateOfBirth.isEmpty() ||
                email.isEmpty() || password.isEmpty()
            ) {
                Toast.makeText(this, "Fyll i alla fält", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Lösenordet måste vara minst 6 tecken", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }


            // Skapa användare med Firebase Auth
            auth.createUserWithEmailAndPassword(
                etEmail.text.toString().trim(),
                etPassword.text.toString().trim()
            )
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                        // Förbered extra data som ska sparas i Firestore
                        val userData = HashMap<String, Any>()
                        userData["username"] = etUsername.text.toString().trim()
                        userData["fullName"] = etFullName.text.toString().trim()
                        userData["dateOfBirth"] = etDateOfBirth.text.toString().trim()
                        userData["email"] = etEmail.text.toString().trim()

                        // Spara till Firestore
                        firestore.collection("users").document(userId)
                            .set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registrering lyckades!", Toast.LENGTH_LONG)
                                    .show()
                                finish()  // Gå tillbaka till föregående skärm
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Fel vid sparande av data: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                    } else {
                        Toast.makeText(
                            this,
                            "Registrering misslyckades: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}