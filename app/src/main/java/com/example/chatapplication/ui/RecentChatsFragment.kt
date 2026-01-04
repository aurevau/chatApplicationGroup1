package com.example.chatapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapplication.databinding.FragmentRecentChatsBinding

class RecentChatsFragment : Fragment() {

    private var _binding: FragmentRecentChatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RecentChatsRecyclerAdapter

    // Use activityViewModels to share data between fragments if needed, or viewModels for just this fragment
    private val viewModel: AllChatsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Skapa adaptern
        adapter = RecentChatsRecyclerAdapter()

        // 2. Koppla RecyclerView till LayoutManager och Adapter
        binding.recyclerViewRecentChats.apply {
            layoutManager = LinearLayoutManager(context)
            // HÄR ÄR FIXEN: Vi måste tilldela adaptern till RecyclerView
            this.adapter = this@RecentChatsFragment.adapter
        }

        // 3. Lyssna på data
        viewModel.recentChats.observe(viewLifecycleOwner) { chatList ->
            adapter.setChats(chatList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
