package com.example.chatapplication.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapplication.adapter.RecentChatsRecyclerAdapter
import com.example.chatapplication.databinding.FragmentRecentChatsBinding
import com.example.chatapplication.popup.DeleteChatPopupFragment
import com.example.chatapplication.repository.MessageRepository
import com.example.chatapplication.viewmodel.AllChatsViewModel
import com.example.chatapplication.viewmodel.UserViewModel

class RecentChatsFragment : Fragment() {

    private var _binding: FragmentRecentChatsBinding? = null
    private val binding get() = _binding!!

    private val messageRepository = MessageRepository()

    private lateinit var adapter: RecentChatsRecyclerAdapter

    // Use activityViewModels to share data between fragments if needed, or viewModels for just this fragment
    private val viewModel: AllChatsViewModel by activityViewModels()

    private lateinit var userViewModel: UserViewModel
    private lateinit var currentUserFullName: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecentChatsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // 1. Create the adapter
        adapter = RecentChatsRecyclerAdapter(
            "",
            onChatClick = { chat ->
                val intent = Intent(requireContext(), ChatActivity::class.java)
                intent.putExtra("ROOM_ID", chat.roomId)
                intent.putExtra("IMAGE_URL", chat.chatRoomImageUrl)

                startActivity(intent)
            },
            onChatLongClick = { chatRoom ->
                DeleteChatPopupFragment.newInstance(chatRoom)
                    .show(parentFragmentManager, "deleteChat")
            }

        )


        // 2. Connect RecyclerView to LayoutManager and Adapter
        binding.recyclerViewRecentChats.apply {
            layoutManager = LinearLayoutManager(context)
            // HERE IS THE FIX: We need to assign the adapter to the RecyclerView
            this.adapter = this@RecentChatsFragment.adapter
        }
        // 3. Listen to data
        viewModel.recentChats.observe(viewLifecycleOwner) { chatList ->
            Log.d("RecentChatsFragment", "recentChats size=${chatList.size}")

            adapter.setChats(chatList)

        }

        val currentUserId = userViewModel.getCurrentUserId() ?: return
        userViewModel.getUserDetailsById(currentUserId) { user ->
            currentUserFullName = user?.fullName ?: ""
            adapter.currentUserFullName = currentUserFullName
            adapter.notifyDataSetChanged()  // uppdatera gruppnamn etc
        }


        viewModel.getRecentChats(requireContext())


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
