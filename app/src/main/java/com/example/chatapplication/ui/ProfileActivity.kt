package com.example.chatapplication.ui

import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.chatapplication.R
import com.example.chatapplication.databinding.ActivityProfileBinding
import com.example.chatapplication.viewmodel.AuthViewModel
import com.example.chatapplication.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView


class ProfileActivity : AppCompatActivity() {

    //private lateinit var ivProfilePicture: CircleImageView
    private var imageUri: Uri? = null
    private var currentUserId: String? = null
    private lateinit var viewModel: UserViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth


    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            Log.d("SOUT", it.toString())
            imageUri = it
            binding.ivProfilePicture.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()


        Log.d("SOUT","ONCREATE")

        binding.backBtn.setOnClickListener {
            finish()
        }


        val friendId = intent.getStringExtra("USER_ID")

        if (friendId == null) {

            binding.ivProfilePicture.setOnClickListener {
                chooseImage()
            }

            binding.btnSave.setOnClickListener {
                save()

            }
        } else {
            binding.btnSave.visibility = View.GONE
            binding.etFullName.editText?.isEnabled = false
            binding.etFullName.editText?.inputType = InputType.TYPE_NULL
            binding.etEmail.editText?.isEnabled = false
            binding.etEmail.editText?.isEnabled = false

        }


        Log.d("SOUT", friendId ?: "")

        viewModel = ViewModelProvider(this)[UserViewModel::class.java]
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]



        currentUserId = friendId ?: viewModel.getCurrentUserId()

        binding.btnOpenFriends.setOnClickListener {
            val friendDialog = FriendFragment()
            val bundle = Bundle()
            bundle.putString("USER_ID", currentUserId)
            friendDialog.arguments = bundle
            friendDialog.show(supportFragmentManager, "friendDialog")
        }

        currentUserId?.let { userId ->

            viewModel.getUserDetailsById(userId = userId) { user ->

                binding.etFullName.editText?.setText(user?.fullName ?: "")
                val email = user?.email ?: auth.currentUser?.email
                binding.etEmail.editText?.setText(email ?: "")
                val imageUrl = user?.profileImageUrl
                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(binding.ivProfilePicture.context)
                        .load(user.profileImageUrl)
                        .circleCrop()
                        .into(binding.ivProfilePicture)
                }


            }
        }

    }

    private fun chooseImage() {
        pickImageLauncher.launch("image/*")
    }

    private fun save() {
        currentUserId?.let { userId ->
            viewModel.getUserDetailsById(userId = userId) { user ->

                if (user != null) {
                    val fullName = user.fullName
                    val email = user.email
                    val newFullName = binding.etFullName.editText?.text.toString()
                    val newEmail = binding.etEmail.editText?.text.toString()
                    if (fullName != newFullName || email != newEmail || imageUri != null) {
                        if (imageUri == null) {
                            authViewModel.saveUserToFirestore(
                                newFullName,
                                newFullName.lowercase(),
                                newEmail,
                                userId,
                                null,
                                onSuccess = {
                                    Toast.makeText(this, "User saved!", Toast.LENGTH_SHORT).show()
                                },
                                onFailure = { errorMessage ->
                                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                                }
                            )
                        } else {
                            authViewModel.uploadProfileImage(
                                imageUri!!,
                                userId,
                                onSuccess = { downloadUrl ->
                                    authViewModel.saveUserToFirestore(
                                        newFullName,
                                        newFullName.lowercase(),
                                        newEmail,
                                        userId,
                                        downloadUrl,
                                        onSuccess = {
                                            Toast.makeText(this, "User saved!", Toast.LENGTH_SHORT).show()
                                        },
                                        onFailure = { errorMessage ->
                                            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                                        }

                                    )


                                },
                                onError = { errorMessage ->
                                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                                })
                        }
                    }
                }



            }
        }



    }
}