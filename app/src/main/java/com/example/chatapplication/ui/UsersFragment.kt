package com.example.chatapplication.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapplication.adapter.SelectedUsersRecyclerAdapter
import com.example.chatapplication.adapter.UserRecyclerAdapter
import com.example.chatapplication.data.User
import com.example.chatapplication.databinding.FragmentUsersBinding
import com.example.chatapplication.viewmodel.UserViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class UsersFragment : Fragment() {

    private lateinit var binding: FragmentUsersBinding

    private lateinit  var adapter: UserRecyclerAdapter

    private lateinit var selectedUsersAdapter: SelectedUsersRecyclerAdapter

    private lateinit var viewModel: UserViewModel

    private lateinit var searchInput: TextInputEditText
    private lateinit var searchButton: FloatingActionButton
    private lateinit var recyclerView: RecyclerView

    private lateinit var rvSelectedUsers: RecyclerView

    private val selectedUsersSet =  mutableSetOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        val currentUserId = viewModel.getCurrentUserId()

        selectedUsersAdapter = SelectedUsersRecyclerAdapter({removedUser ->
            selectedUsersSet.remove(removedUser)
            viewModel.isNotSelected(currentUserId, removedUser.id)

            selectedUsersAdapter.submitList(selectedUsersSet.toList())
            adapter.notifyDataSetChanged()

            binding.btnStartGroupChat.visibility = if (selectedUsersSet.size > 1) View.VISIBLE else View.GONE
            binding.rvSelectedUsers.visibility = if (selectedUsersSet.isNotEmpty()) View.VISIBLE else View.GONE



        })

        adapter = UserRecyclerAdapter( viewModel, {user->
            // Se mer information om användaren och kunna lägga till vän?
            binding.cvSearchUser.visibility = View.GONE
            binding.etSearchUser.text?.clear()
            viewModel.addRecentSearch(user)

        }, {user ->
            // Start New chatroom from user or open existing chatroom. Need ChatRoomRepository for this!
            val chatIntent = Intent(activity, ChatActivity::class.java)
            chatIntent.putExtra("USER_ID", user.id)
            startActivity(chatIntent)

        }, { user ->
            viewModel.addFriend(currentUserId, user)
        }, {user ->
            viewModel.removeFriend(currentUserId, user.id)
        }, {user, isChecked ->
            if (isChecked) {
                selectedUsersSet.add(user)
                viewModel.isSelected(currentUserId, user)
            }

            else {selectedUsersSet.remove(user)
                viewModel.isNotSelected(currentUserId, user.id)
            }

            selectedUsersAdapter.submitList(selectedUsersSet.toList())
            if(selectedUsersSet.size <= 1) Toast.makeText(requireContext(), "Choose another user to start group chat", Toast.LENGTH_SHORT).show()
            binding.btnStartGroupChat.visibility = if (selectedUsersSet.size > 1) View.VISIBLE else View.GONE
            binding.rvSelectedUsers.visibility = if (selectedUsersSet.size > 1) View.VISIBLE else View.GONE
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

        viewModel.loadRecentSearches()


        viewModel.recentSearchedUsers.observe(viewLifecycleOwner) {recentSearchList ->
            adapter.submitList(recentSearchList)
        }

        viewModel.searchResults.observe(viewLifecycleOwner) { searchList ->
            adapter.submitList(searchList)
        }


        viewModel.friends.observe(viewLifecycleOwner) {friendsList ->

            adapter.updateFriendList(friendsList)
        }

        viewModel.selection.observe(viewLifecycleOwner) {selectionList ->

            adapter.updateSelectionList(selectionList)
            val selectedUsers = adapter.getSelectedUsers()
            selectedUsersAdapter.submitList(selectedUsers.toList())
            // Visa/hide knappar
            binding.btnStartGroupChat.visibility = if (selectedUsers.size > 1) View.VISIBLE else View.GONE
            binding.rvSelectedUsers.visibility = if (selectedUsers.isNotEmpty()) View.VISIBLE else View.GONE
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

            if(query.isNotEmpty()){
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