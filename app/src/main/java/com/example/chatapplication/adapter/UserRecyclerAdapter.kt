package com.example.chatapplication.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapplication.data.User
import com.example.chatapplication.databinding.ListItemUserBinding
import com.example.chatapplication.repository.UserRepository
import com.example.chatapplication.viewmodel.UserViewModel

class UserRecyclerAdapter(
    private val viewModel: UserViewModel,
    val onItemClick: (User) -> Unit,
    val onButtonClick: (User) -> Unit,
    val onAddFriendClick: (User) -> Unit,
    val onDeleteFriendClick: (User) -> Unit,
    val onCheckButtonClick: (User, Boolean) -> Unit,
    val onItemLongClick: (User) -> Unit,
) : RecyclerView.Adapter<UserRecyclerAdapter.UserViewHolder>() {


//    private val selectedUsers = mutableListOf<User>()

    private var users = emptyList<User>()
    private val db = UserRepository()


    private var friends = emptyList<User>()

    private var selection = emptyList<User>()

    private val selectedUsersSet = mutableSetOf<User>()


    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): UserRecyclerAdapter.UserViewHolder {
        val binding = ListItemUserBinding.inflate(
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

    fun updateSelectionList(newSelection: List<User>) {
        selection = newSelection
        notifyDataSetChanged()
    }

    fun submitList(userList: List<User>) {
        users = userList
        notifyDataSetChanged()
    }

    fun getSelectedUsers(): List<User> = selectedUsersSet.toList()

    override fun onBindViewHolder(
        holder: UserRecyclerAdapter.UserViewHolder, position: Int
    ) {

        val user = users[position]

        Log.d(
            "PROFILE_IMG",
            "User=${user.fullName}, imageUrl=${user.profileImageUrl}"
        )


        val isSelected = selection.any { it.id == user.id }


        holder.binding.checkbox.setOnCheckedChangeListener(null)
        holder.binding.checkbox.isChecked = isSelected
        holder.binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            onCheckButtonClick(user, isChecked)
        }

        val isFriend = friends.any { it.id == user.id }

        holder.binding.tvAddFriend.visibility = if (isFriend) View.GONE else View.VISIBLE
        holder.binding.tvDeleteFriend.visibility = if (isFriend) View.VISIBLE else View.GONE


        holder.binding.tvAddFriend.setOnClickListener {
            onAddFriendClick(user)
        }

        holder.binding.tvDeleteFriend.setOnClickListener {
            onDeleteFriendClick(user)
        }


        val imageUrl = user.profileImageUrl

        if (!imageUrl.isNullOrEmpty()) {
            holder.binding.tvInitials.visibility = View.INVISIBLE
            holder.binding.profilePic.visibility = View.VISIBLE

            Glide.with(holder.binding.profilePic.context)
                .load(imageUrl)
                .circleCrop()
                .into(holder.binding.profilePic)
        } else {
            holder.binding.tvInitials.visibility = View.VISIBLE
            holder.binding.profilePic.visibility = View.GONE
            holder.binding.tvInitials.text = user.initials
        }

        holder.binding.tvName.text = if (user.id == db.getCurrentUserId()) {
            "${user.fullName} (Me)"
        } else {
            user.fullName
        }


        holder.binding.root.setOnLongClickListener {
            onItemLongClick(user)
            true
        }


        holder.binding.root.setOnClickListener {
            onItemClick(user)
        }

        holder.binding.btnStartChat.setOnClickListener {
            onButtonClick(user)
        }
    }


    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(val binding: ListItemUserBinding) : RecyclerView.ViewHolder(binding.root) {

    }
}