package com.example.chatapplication.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapplication.adapter.UserRecyclerAdapter
import com.example.chatapplication.databinding.FragmentUsersBinding
import com.example.chatapplication.viewmodel.UserViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class UsersFragment : Fragment() {

    private lateinit var binding: FragmentUsersBinding

    private lateinit  var adapter: UserRecyclerAdapter

    private lateinit var viewModel: UserViewModel

    private lateinit var searchInput: TextInputEditText
    private lateinit var searchButton: FloatingActionButton
    private lateinit var recyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        val currentUserId = viewModel.getCurrentUserId()

        adapter = UserRecyclerAdapter( viewModel, {user ->
            // Se mer information om användaren och kunna lägga till vän?

        }, {user ->
            // Start New chatroom from user or open existing chatroom. Need ChatRoomRepository for this!

        }, { user ->
            viewModel.addFriend(currentUserId, user)
        }, {user ->
            viewModel.removeFriend(currentUserId, user.id)
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

        searchButton = binding.btnSearchUser
        searchInput = binding.etSearchUser

        val currentUserId = viewModel.getCurrentUserId() ?: return

        viewModel.user.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)

        }


        viewModel.friends.observe(viewLifecycleOwner) {friendsList ->
            adapter.updateFriendList(friendsList)
        }

        viewModel.getFriends(currentUserId)



        searchButton.setOnClickListener {

            binding.cvSearchUser.visibility = View.VISIBLE

            val searchTerm = searchInput.text.toString()

//
            viewModel.searchUsers(searchTerm)



        }
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }


}