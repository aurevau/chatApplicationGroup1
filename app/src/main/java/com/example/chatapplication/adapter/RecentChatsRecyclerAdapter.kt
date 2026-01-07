package com.example.chatapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapplication.data.ChatRoom
import com.example.chatapplication.R
import com.example.chatapplication.databinding.ItemRecentChatBinding

class RecentChatsRecyclerAdapter(
    private val onChatClick: (ChatRoom) -> Unit,
    val onChatLongClick: (ChatRoom) -> Unit,
) : RecyclerView.Adapter<RecentChatsRecyclerAdapter.ChatViewHolder>() {

    private var chats = emptyList<ChatRoom>()

    // Update the list when data comes from the ViewModel
    fun setChats(newChats: List<ChatRoom>) {
        chats = newChats
        notifyDataSetChanged()  // Update RecyclerView
    }

    // How many rows?
    override fun getItemCount() = chats.size

    // Create a row (use item_recent_chat.xml)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemRecentChatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding)
    }

    // Fill data in each row
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chats[position], onChatClick, onChatLongClick)
    }

    // ViewHolder = holds widgets in each row
    class ChatViewHolder(val binding: ItemRecentChatBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            chat: ChatRoom,
            onChatClick: (ChatRoom) -> Unit,
            onChatLongClick: (ChatRoom) -> Unit
        ) {

            binding.root.setOnLongClickListener {
                onChatLongClick(chat)
                true
            }

            // Show group icon or initials based on chat type
            if(chat.isGroup) {
                binding.ivChatIcon.setImageResource(R.drawable.group_icon)
                binding.ivChatIcon.visibility = View.VISIBLE
                binding.flChatIcon.visibility = View.VISIBLE
                binding.tvProfileInitials.visibility = View.GONE
            } else {
                binding.ivChatIcon.visibility = View.GONE
                binding.flChatIcon.visibility = View.GONE
                binding.tvProfileInitials.visibility = View.VISIBLE
            }

            binding.tvProfileInitials.text = chat.userName?.take(2)  // First 2 letters of name
            binding.tvChatName.text = chat.userName ?: "Unknown"
            binding.tvLastMessage.text = chat.lastMessage ?: "No Message"
            binding.tvTimestamp.text = chat.timestamp ?: "Now"

            binding.root.setOnClickListener {
                onChatClick(chat)
            }
        }
    }
}