package com.example.chatapplication.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapplication.R
import com.example.chatapplication.databinding.ActivityChatBinding
import com.example.chatapplication.adapter.ChatRecyclerAdapter
import com.example.chatapplication.viewmodel.AuthViewModel
import com.example.chatapplication.viewmodel.ChatViewModel
import java.io.File


class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var spinner: AppCompatSpinner

    private lateinit var authViewModel: AuthViewModel

    private val CAMERA_REQUEST_CODE = 1001
    private val GALLERY_REQUEST_CODE = 1002
    private var cameraImageUri: Uri? = null


    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)



        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]


        spinner = binding.menuSpinner
        val menuCategories = resources.getStringArray(R.array.menu_spinner)

        val spinnerAdapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            menuCategories
        )
        spinner.adapter = spinnerAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) return
                if (menuCategories[position] == "Logout") {
                    authViewModel.logOut()

                    // Starta WelcomeActivity med CLEAR_TASK
                    val intent = Intent(this@ChatActivity, WelcomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)


                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val userId = intent.getStringExtra("USER_ID")
        val groupRoomId = intent.getStringExtra("ROOM_ID")

        val groupName = intent.getStringExtra("GROUP_NAME")

        val currentRoomId = groupRoomId ?: run {
            val myId = viewModel.myUserId ?: ""
            listOf(myId, userId ?: "").sorted().joinToString("_")

        }

        if (!groupName.isNullOrEmpty()) {
            binding.tvHeader.text = groupName
            binding.tvInitials.visibility = View.GONE
        } else if (userId != null) {
            viewModel.getUserDetailsById(userId)
            viewModel.targetUser.observe(this) { user ->
                binding.tvHeader.text = user?.fullName
                binding.tvInitials.text = user?.initials
            }
        }


        viewModel.start(currentRoomId)


        val adapter = ChatRecyclerAdapter()
        binding.recyclerMessages.adapter = adapter
        binding.recyclerMessages.layoutManager = LinearLayoutManager(this).apply {
            reverseLayout = false
            stackFromEnd = false
        }
        viewModel.messages.observe(this) { messageList ->
            adapter.submitList(messageList) {
                // Scroll after the list is submitted and laid out
                if (messageList.isNotEmpty()) {
                    binding.recyclerMessages.post {
                        binding.recyclerMessages.scrollToPosition(messageList.size - 1)
                    }
                }
            }
        }

        binding.btnSend.setOnClickListener {
            val roomId = currentRoomId ?: return@setOnClickListener
            val text = binding.etMessage.text.toString()
            binding.etMessage.text.clear()
            val selectedImage = viewModel.selectedImageUri.value

            if (selectedImage != null) {
                binding.progressCircular.visibility = View.VISIBLE
                viewModel.uploadChatImage(
                    selectedImage, roomId,
                    onSuccess = { imageUrl ->
                        viewModel.sendImageMessage(roomId, imageUrl, text)
                        viewModel.selectedImageUri.value = null
                        binding.ivPhoto.visibility = View.GONE
                        binding.progressCircular.visibility = View.GONE
                    },
                    onError = { e ->
                        Toast.makeText(
                            this,
                            "Failed to send image: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            } else if (text != null) {
                viewModel.sendTextMessage(roomId, text)
                binding.etMessage.text.clear()
            }
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnImage.setOnClickListener {
            requestMediaPermissions()
        }


    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.values.all { it }
            if (granted) {
                showImageSourceDialog()
            } else {
                Toast.makeText(this, "Permission required to select images", Toast.LENGTH_SHORT)
                    .show()
            }
        }


    private fun requestMediaPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            )
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
        }
    }


    private fun showImageSourceDialog() {
        val options = arrayOf("Camera", "Gallery")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select Image Source")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }.show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun openCamera() {
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        val file = File(externalCacheDir, "chat_image_${System.currentTimeMillis()}.jpg")
        cameraImageUri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, cameraImageUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        val imageUri: Uri? = when (requestCode) {
            CAMERA_REQUEST_CODE -> cameraImageUri
            GALLERY_REQUEST_CODE -> data?.data
            else -> null
        }

        imageUri?.let {
            viewModel.selectedImageUri.value = it
            binding.ivPhoto.setImageURI(it)
            binding.ivPhoto.visibility = View.VISIBLE

        }


    }


}