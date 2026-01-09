package com.example.chatapplication.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapplication.R
import com.example.chatapplication.adapter.SelectedUsersRecyclerAdapter
import com.example.chatapplication.adapter.UserRecyclerAdapter
import com.example.chatapplication.data.User
import com.example.chatapplication.databinding.FragmentUsersBinding
import com.example.chatapplication.repository.UserRepository
import com.example.chatapplication.viewmodel.ChatViewModel
import com.example.chatapplication.viewmodel.UserViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class UsersFragment : Fragment() {

    private lateinit var binding: FragmentUsersBinding

    private lateinit var adapter: UserRecyclerAdapter

    private lateinit var selectedUsersAdapter: SelectedUsersRecyclerAdapter

    private lateinit var viewModel: UserViewModel
    private lateinit var chatViewModel: ChatViewModel

    private lateinit var searchInput: TextInputEditText
    private lateinit var searchButton: FloatingActionButton
    private lateinit var recyclerView: RecyclerView

    private lateinit var rvSelectedUsers: RecyclerView

    private val selectedUsersSet = mutableSetOf<User>()

    private lateinit var groupChatButton: Button
    private val userRepository = UserRepository()

    val incomingRequests = mutableSetOf<String>()
    val outgoingRequests = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        chatViewModel = ViewModelProvider(requireActivity())[ChatViewModel::class.java]

        val currentUserId = viewModel.getCurrentUserId()
        viewModel.loadRecentSearches()

        selectedUsersAdapter = SelectedUsersRecyclerAdapter({ removedUser ->
            selectedUsersSet.remove(removedUser)
            viewModel.isNotSelected(currentUserId, removedUser.id)

            selectedUsersAdapter.submitList(selectedUsersSet.toList())
            adapter.notifyDataSetChanged()

            groupChatButton.visibility = if (selectedUsersSet.size > 1) View.VISIBLE else View.GONE
            rvSelectedUsers.visibility =
                if (selectedUsersSet.isNotEmpty()) View.VISIBLE else View.GONE


        })

        adapter = UserRecyclerAdapter(viewModel, { user ->
            // See more information about the user and be able to add friends?
            binding.cvSearchUser.visibility = View.GONE
            binding.etSearchUser.text?.clear()
            if (currentUserId != null) {
                viewModel.addRecentSearchToFirebase(currentUserId, user)
            }

        }, { user ->
            // Start New chatroom from user or open existing chatroom. Need ChatRoomRepository for this!
            val chatIntent = Intent(activity, ChatActivity::class.java)
            chatIntent.putExtra("USER_ID", user.id)
            startActivity(chatIntent)
            binding.etSearchUser.text?.clear()

        }, { user ->

            if (user.id !in outgoingRequests) {
                userRepository.getUserDetailsById(currentUserId!!) { currentUser ->
                    if (currentUser != null) {
                        viewModel.sendFriendRequest(
                            fromUserId = currentUserId,
                            fromUserName = currentUser.fullName,
                            toUserId = user.id!!,
                            toUserName = user.fullName
                        )
                    } else {
                        Log.e("FRIEND_REQUEST", "Could not fetch current user details")
                    }
                }
            }


        }, { user ->

            AlertDialog.Builder(context)
                .setTitle(getString(R.string.delete_friend_text))
                .setMessage(getString(R.string.delete_friend_alert_text, user.fullName))
                .setPositiveButton(getString(R.string.confirm_delete_btn_text)) { dialog, _ ->
                    viewModel.removeFriend(currentUserId, user)
                    dialog.dismiss()
                }

                .setNegativeButton(getString(R.string.cancel_alert_btn_text)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }, { user, isChecked ->
            if (isChecked) {
                selectedUsersSet.add(user)
                viewModel.isSelected(currentUserId, user)
            } else {
                selectedUsersSet.remove(user)
                viewModel.isNotSelected(currentUserId, user.id)
            }

            selectedUsersAdapter.submitList(selectedUsersSet.toList())
            if (selectedUsersSet.size <= 1) Toast.makeText(
                requireContext(),
                getString(R.string.start_group_chat_text),
                Toast.LENGTH_SHORT
            ).show()
            binding.btnStartGroupChat.visibility =
                if (selectedUsersSet.size > 1) View.VISIBLE else View.GONE
            binding.rvSelectedUsers.visibility =
                if (selectedUsersSet.size > 1) View.VISIBLE else View.GONE
        }, { user ->
            AlertDialog.Builder(context)
                .setTitle(getString(R.string.remove_user_from_recent))
                .setMessage(getString(R.string.confirm_remove, user.fullName))
                .setPositiveButton(getString(R.string.yes_remove)) { dialog, _ ->
                    userRepository.deleteRecentSearch(user)
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }, { user ->

            AlertDialog.Builder(context)
                .setTitle(getString(R.string.cancel_request_btn_text))
                .setMessage(getString(R.string.confirm_cancel_friend_request, user.fullName))
                .setPositiveButton(getString(R.string.yes_cancel_btn_text)) { dialog, _ ->
                    val currentUserId = viewModel.getCurrentUserId() ?: return@setPositiveButton
                    viewModel.cancelOutgoingFriendRequest(currentUserId, user.id!!)
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.keep_request_btn_text)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()

        }, { user ->
            val currentUser = viewModel.getUserDetailsById(currentUserId!!) { currentUser ->
                if (currentUser != null) {
                    viewModel.acceptFriendRequest(
                        currentUserId, user.id!!,
                        currentUser.fullName, user.fullName
                    )
                }
            }


        }, { user ->
            AlertDialog.Builder(context)
                .setTitle(getString(R.string.decline_friend_request_alert_text))
                .setMessage(getString(R.string.sent_friend_request_text, user.fullName))
                .setPositiveButton(R.string.decline_friend_request_btn_text) { dialog, _ ->
                    viewModel.declineFriendRequest(currentUserId!!, user.id!!)
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.cancel_alert_btn_text) { dialog, _ ->
                    dialog.dismiss()

                }
                .show()
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.rvUsers
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        rvSelectedUsers = binding.rvSelectedUsers
        rvSelectedUsers.layoutManager = GridLayoutManager(requireContext(), 2)
        rvSelectedUsers.adapter = selectedUsersAdapter

        searchButton = binding.btnSearchUser
        searchInput = binding.etSearchUser

        val currentUserId = viewModel.getCurrentUserId() ?: return

//        viewModel.loadRecentSearches()

        if (currentUserId != null) {
            viewModel.loadIncomingFriendRequests(currentUserId)
            viewModel.loadOutgoingFriendRequests(currentUserId)
        }

        groupChatButton = binding.btnStartGroupChat

        groupChatButton.setOnClickListener {
            val currentUserId = viewModel.getCurrentUserId() ?: return@setOnClickListener
            val selectedUsers = viewModel.selection.value ?: emptyList()


            val memberIds = (selectedUsers.mapNotNull { it.id } + currentUserId).sorted()
            val groupName = selectedUsers
                .filter { it.id != currentUserId }
                .joinToString(", ") { it.fullName.substringBefore(" ") }



            chatViewModel.createGroupChat(
                roomId = memberIds.joinToString("_"),
                userIds = memberIds,
                groupName = groupName
            ) { roomId ->
                val chatIntent = Intent(requireContext(), ChatActivity::class.java)
                chatIntent.putExtra("ROOM_ID", roomId)
                chatIntent.putExtra("GROUP_NAME", groupName)
                startActivity(chatIntent)


                viewModel.clearSelection()
                selectedUsersSet.clear()
                adapter.updateSelectionList(viewModel.selection.value ?: emptyList())
                selectedUsersAdapter.submitList(emptyList())
                binding.rvSelectedUsers.visibility = View.GONE
                binding.btnStartGroupChat.visibility = View.GONE
            }

            binding.etSearchUser.text?.clear()
        }


        viewModel.recentSearchedUsers.observe(viewLifecycleOwner) { recentSearchList ->
            adapter.submitList(recentSearchList)
        }

        viewModel.searchResults.observe(viewLifecycleOwner) { searchList ->
            adapter.submitList(searchList)
        }

        viewModel.incomingFriendRequest.observe(viewLifecycleOwner) { requests ->
            val incomingIds = requests.mapNotNull { it.id }.toSet()
            val outgoingIds =
                viewModel.outgoingFriendRequest.value?.mapNotNull { it.id }?.toSet() ?: emptySet()
            adapter.updateFriendRequestStatus(incomingIds, outgoingIds)

        }

        viewModel.outgoingFriendRequest.observe(viewLifecycleOwner) { requests ->
            val outgoingIds = requests.mapNotNull { it.id }.toSet()
            val incomingIds =
                viewModel.incomingFriendRequest.value?.mapNotNull { it.id }?.toSet() ?: emptySet()
            adapter.updateFriendRequestStatus(incomingIds, outgoingIds)
        }


        viewModel.friends.observe(viewLifecycleOwner) { friendsList ->

            adapter.updateFriendList(friendsList)
            Log.d("FRIENDS_OBSERVED", "Updated friends: ${friendsList.map { it.fullName }}")
        }

        viewModel.selection.observe(viewLifecycleOwner) { selectionList ->
            if (selectionList != null) {
                adapter.updateSelectionList(selectionList)
                selectedUsersAdapter.submitList(selectionList)
                binding.btnStartGroupChat.visibility = if (selectionList.size > 1) View.VISIBLE else View.GONE
                binding.rvSelectedUsers.visibility = if (selectionList.isNotEmpty()) View.VISIBLE else View.GONE
            }


        }

        viewModel.getFriends(currentUserId)

        searchButton.setOnClickListener {
            binding.cvSearchUser.visibility = View.VISIBLE

            val searchTerm = searchInput.text.toString()
            if (searchTerm.isNotEmpty()) {
                viewModel.searchUsers(searchTerm)
            }
        }

        searchInput.addTextChangedListener { text ->
            val query = text.toString().trim()

            if (query.isNotEmpty()) {
                viewModel.searchUsers(query)
            } else {
                val recent = viewModel.recentSearchedUsers.value ?: emptyList()
                adapter.submitList(recent)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        adapter.notifyDataSetChanged()
    }
}