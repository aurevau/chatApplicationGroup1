package com.example.chatapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapplication.viewmodel.AllChatsViewModel
import com.example.chatapplication.adapter.RecentChatsRecyclerAdapter
import com.example.chatapplication.databinding.FragmentRecentChatsBinding

class RecentChatsFragment : Fragment() {

    private var _binding: FragmentRecentChatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RecentChatsRecyclerAdapter

    // Use activityViewModels to share data between fragments if needed, or viewModels for just this fragment
    private lateinit var viewModel: AllChatsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RecentChatsRecyclerAdapter()

        viewModel = ViewModelProvider(this)[AllChatsViewModel::class.java]

        binding.recyclerViewRecentChats.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@RecentChatsFragment.adapter
        }

        viewModel.recentChats.observe(viewLifecycleOwner) { chatList ->
            adapter.setChats(chatList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}