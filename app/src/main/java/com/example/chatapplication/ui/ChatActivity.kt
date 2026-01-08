package com.example.chatapplication.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chatapplication.R
import com.example.chatapplication.adapter.ChatRecyclerAdapter
import com.example.chatapplication.databinding.ActivityChatBinding
import com.example.chatapplication.viewmodel.AuthViewModel
import com.example.chatapplication.viewmodel.ChatViewModel
import java.io.File


class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
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


        binding.dropdownMenu.setOnClickListener {
            val wrapper = ContextThemeWrapper(this, R.style.CustomPopupMenu)
            val popupMenu = PopupMenu(wrapper, it)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_profile -> {
                        val intent = Intent(this@ChatActivity, ProfileActivity::class.java)
                        startActivity(intent)
                        true
                    }

                    R.id.menu_logout -> {
                        authViewModel.logOut()

                        // Start WelcomeActivity with CLEAR_TASK
                        val intent = Intent(this@ChatActivity, WelcomeActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        true
                    }

                    else -> false
                }
            }

            popupMenu.inflate(R.menu.menu_dropdown)

            try {
                val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                fieldMPopup.isAccessible = true
                val mPopup = fieldMPopup.get(popupMenu)
                mPopup.javaClass
                    .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                    .invoke(mPopup, true)
            } catch (e: Exception) {
                Log.e("SOUT", "Error showing menu icon")
            } finally {
                popupMenu.show()
            }
        }

        val userId = intent.getStringExtra("USER_ID")
        val groupRoomId = intent.getStringExtra("ROOM_ID")
        val imageUrl = intent.getStringExtra("IMAGE_URL")

        val groupName = intent.getStringExtra("GROUP_NAME")

        val currentRoomId = groupRoomId ?: run {
            val myId = viewModel.myUserId ?: ""
            listOf(myId, userId ?: "").sorted().joinToString("_")

        }


        if (!imageUrl.isNullOrEmpty()) {
            binding.tvInitials.visibility = View.GONE
            binding.profilePic.visibility = View.VISIBLE
            Glide.with(binding.profilePic.context)
                .load(imageUrl)
                .circleCrop()
                .into(binding.profilePic)
        } else {
            binding.tvInitials.visibility = View.GONE
        }
        if (userId != null) {
            viewModel.getUserDetailsById(userId)
            viewModel.targetUser.observe(this) { user ->
                Log.d("??? user", user.toString())
                binding.tvInitials.visibility = View.VISIBLE
                binding.tvHeader.text = user?.fullName
                binding.tvInitials.text = user?.initials
            }
        } else if (!groupName.isNullOrEmpty()) {
            Log.d("??? group", groupName)
            binding.tvHeader.text = groupName
            //binding.tvInitials.visibility = View.GONE

        }


        viewModel.start(currentRoomId)


        val adapter = ChatRecyclerAdapter { message ->
            android.app.AlertDialog.Builder(this)
                .setMessage(getString(R.string.delete_message_alert_text))
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    viewModel.deleteMessage(message.id, message.roomId, message.senderId)
                }
                .setNegativeButton(getString(R.string.no), null)
                .show()
        }
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
                        viewModel.sendImageMessage(roomId, imageUrl, text, userId)
                        viewModel.selectedImageUri.value = null
                        binding.ivPhoto.visibility = View.GONE
                        binding.progressCircular.visibility = View.GONE
                    },
                    onError = { e ->
                        Toast.makeText(
                            this,
                            getString(R.string.failed_to_send_image, e.message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            } else if (text != null) {
                viewModel.sendTextMessage(roomId, text, userId)
                binding.etMessage.text.clear()
            }
        }

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
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
                Toast.makeText(this, getString(R.string.permission_for_images), Toast.LENGTH_SHORT)
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
            .setTitle(getString(R.string.select_image_source))
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