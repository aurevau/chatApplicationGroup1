package com.example.chatapplication.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.chatapplication.databinding.FragmentChangeChatNameBinding
import com.example.chatapplication.viewmodel.ChatViewModel
import com.example.chatapplication.viewmodel.UserViewModel
import com.google.android.material.textfield.TextInputLayout

class ChangeChatNameFragment : DialogFragment() {

    private lateinit var binding: FragmentChangeChatNameBinding

    private lateinit var chatViewModel: ChatViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var currentUserFullName: String


    private lateinit var etChangeName: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatViewModel = ViewModelProvider(requireActivity())[ChatViewModel::class.java]
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChangeChatNameBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val state = arguments?.getString("state")
        val roomId = arguments?.getString("ROOM_ID")
        val memberIds =
            arguments?.getStringArrayList("member_ids") ?: emptyList()
        val selectedUserNames = arguments?.getStringArrayList("selected_users") ?: emptyList()
        etChangeName = binding.etChatName

        val currentUserId = userViewModel.getCurrentUserId()
        if (currentUserId != null ){
            userViewModel.getUserDetailsById(currentUserId) {user ->
                currentUserFullName = user?.fullName ?: return@getUserDetailsById

                val allMemberNames = mutableListOf<String>()
                currentUserFullName.let { allMemberNames.add(it) }  // Lägg till dig själv först
                allMemberNames.addAll(selectedUserNames)



                binding.buttonSaveChangeName.setOnClickListener {
                    val inputName = etChangeName.editText?.text?.toString()?.trim()

                    if (state == "UPDATE") {
                        if (roomId != null) {
                            if (inputName != null) {
                                chatViewModel.updateChatName(roomId, inputName)
                                dismiss()
                                return@setOnClickListener
                            }
                        }
                    }

                    val groupName = inputName ?: ""

                    chatViewModel.createGroupChat(
                        roomId = memberIds.joinToString("_"),
                        userIds = memberIds,
                        groupName = groupName,
                        memberNames = allMemberNames

                    ) { roomId ->
                        val chatIntent = Intent(requireContext(), ChatActivity::class.java)
                        chatIntent.putExtra("ROOM_ID", roomId)
                        startActivity(chatIntent)
                        dismiss()
                    }





                }
        }




        }


    }



}