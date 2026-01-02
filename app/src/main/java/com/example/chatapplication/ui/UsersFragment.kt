package com.example.chatapplication.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapplication.R
import com.example.chatapplication.adapter.UserRecyclerAdapter
import com.example.chatapplication.databinding.FragmentUsersBinding
import com.example.chatapplication.viewmodel.UserViewModel

class UsersFragment : Fragment() {

    private lateinit var binding: FragmentUsersBinding

    private lateinit  var adapter: UserRecyclerAdapter

    private lateinit var viewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        adapter = UserRecyclerAdapter({user ->
            // Start New chatroom from user or open existing chatroom. Need ChatRoomRepository for this!
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

        binding.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsers.adapter = adapter

        viewModel.user.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)

        }
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }


}