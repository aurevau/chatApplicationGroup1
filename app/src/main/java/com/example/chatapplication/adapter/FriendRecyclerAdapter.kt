package com.example.chatapplication.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapplication.data.User
import com.example.chatapplication.databinding.ListItemFriendsBinding

class FriendRecyclerAdapter(
    val onItemClick: (User) -> Unit,
    val onAddFriendClick: (User) -> Unit,
    val onDeleteFriendClick: (User) -> Unit,
    val onProfileClick: (User) -> Unit
): RecyclerView.Adapter<FriendRecyclerAdapter.UserViewHolder>() {

    private var friends = emptyList<User>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FriendRecyclerAdapter.UserViewHolder {
        val binding = ListItemFriendsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    fun updateFriendList(newFriends: List<User>) {
        Log.d("Adapter", "Friends list updated: ${newFriends.map { it.fullName }}")
        friends = newFriends
        notifyDataSetChanged()
    }

    fun submitList(friendList: List<User>) {
        friends = friendList
        notifyDataSetChanged()
    }

    // holder: ViewHolder object that holds references to views for ONE row in RecyclerView
    override fun onBindViewHolder(
        holder: FriendRecyclerAdapter.UserViewHolder,
        position: Int
    ) {
        val friend = friends[position]

        holder.binding.tvDeleteFriendFriend.visibility = View.VISIBLE

        holder.binding.tvAddFriendFriend.setOnClickListener {
            onAddFriendClick(friend)
        }

        holder.binding.tvDeleteFriendFriend.setOnClickListener {
            onDeleteFriendClick(friend)
        }

        holder.binding.root.setOnClickListener {
            onItemClick(friend)
        }

        holder.binding.tvInitialsFriend.setOnClickListener {
            onProfileClick(friend)
        }

        holder.binding.tvInitialsFriend.text = friend.initials.ifBlank { "?" }
        holder.binding.tvNameFriend.text = friend.fullName
    }

    override fun getItemCount(): Int = friends.size

    inner class UserViewHolder(val binding: ListItemFriendsBinding): RecyclerView.ViewHolder(binding.root) {

    }
}