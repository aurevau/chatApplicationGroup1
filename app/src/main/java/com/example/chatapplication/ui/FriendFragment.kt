package com.example.chatapplication.ui
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapplication.adapter.FriendRecyclerAdapter
import com.example.chatapplication.data.User
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
            if (currentUserId != null) {
                viewModel.loadIncomingFriendRequests(currentUserId)
                viewModel.loadOutgoingFriendRequests(currentUserId)
            }
        }



        adapter = FriendRecyclerAdapter({user ->
            val chatIntent = Intent(activity, ChatActivity::class.java)
            chatIntent.putExtra("USER_ID", user.id)
            startActivity(chatIntent)
        }, {user ->
            AlertDialog.Builder(context)
                .setTitle("Delete friend")
                .setMessage("Are you sure you want to delete friend: ${user.fullName} ")
                .setPositiveButton("Yes, delete") { dialog, _ ->
                    viewModel.removeFriend(currentUserId, user)
                    dialog.dismiss()
                }

                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }, {user ->
            val profileIntent = Intent(activity, ProfileActivity::class.java)
            profileIntent.putExtra("USER_ID", user.id)
            startActivity(profileIntent)
        }, {user ->
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
                .setTitle("Decline Request")
                .setMessage("${user.fullName} sent you a friend request")
                .setPositiveButton("Decline") { dialog, _ ->
                    viewModel.declineFriendRequest(currentUserId!!, user.id!!)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()

                }
                .show()
        })

        if (currentUserId != null) {
            viewModel.loadOutgoingFriendRequests(currentUserId)
            viewModel.loadIncomingFriendRequests(currentUserId)
        }







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

        val currentUserId = viewModel.getCurrentUserId() ?: return




        viewModel.friends.observe(viewLifecycleOwner) {friendList ->
            adapter.updateFriends(friendList)
        }

        viewModel.incomingFriendRequest.observe(viewLifecycleOwner) { requests ->
            adapter.updateIncomingRequests(requests)

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