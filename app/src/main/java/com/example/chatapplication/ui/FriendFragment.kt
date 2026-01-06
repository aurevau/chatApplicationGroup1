package com.example.chatapplication.ui
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapplication.adapter.FriendRecyclerAdapter
import com.example.chatapplication.databinding.FragmentFriendBinding
import com.example.chatapplication.viewmodel.UserViewModel


class FriendFragment : DialogFragment() {


    private lateinit var viewModel: UserViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var binding: FragmentFriendBinding

    private lateinit var adapter: FriendRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]

        val currentUserId = arguments?.getString("USER_ID")
        //val currentUserId = viewModel.getCurrentUserId()
        if (currentUserId != null) {
            viewModel.getFriends(currentUserId)
        }

        adapter = FriendRecyclerAdapter({user ->
            val chatIntent = Intent(activity, ChatActivity::class.java)
            chatIntent.putExtra("USER_ID", user.id)
            startActivity(chatIntent)
        }, {user ->
            viewModel.addFriend(currentUserId, user)
        }, {user ->
            viewModel.removeFriend(currentUserId, user)
        }, {user ->
            val profileIntent = Intent(activity, ProfileActivity::class.java)
            profileIntent.putExtra("USER_ID", user.id)
            startActivity(profileIntent)
        })


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        binding = FragmentFriendBinding.inflate(inflater, container, false)

        val btnClose = binding.btnClose
        btnClose.setOnClickListener {
            dismiss()
        }
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.rvFriends
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter


        viewModel.friends.observe(viewLifecycleOwner) {friendList ->
            adapter.updateFriendList(friendList)
        }
    }





    override fun onStart() {
        super.onStart()
        if (dialog != null && dialog!!.window != null) {
            dialog!!.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

}