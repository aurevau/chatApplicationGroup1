package com.example.chatapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapplication.data.User
import com.example.chatapplication.databinding.ListItemSelectedUsersBinding
import com.example.chatapplication.repository.UserRepository

class SelectedUsersRecyclerAdapter(val onItemClick: (User) -> Unit) :
    RecyclerView.Adapter<SelectedUsersRecyclerAdapter.UserViewHolder>() {

    private var users = listOf<User>()
    private val db = UserRepository()


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SelectedUsersRecyclerAdapter.UserViewHolder {
        val binding = ListItemSelectedUsersBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    fun submitList(userList: List<User>) {
        users = userList
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(
        holder: SelectedUsersRecyclerAdapter.UserViewHolder,
        position: Int
    ) {
        val user = users[position]
        holder.binding.tvNameSelectedUsers.text = user.fullName
        holder.binding.tvInitialsSelectedUsers.text = user.initials

        holder.binding.root.setOnClickListener {
            onItemClick(user)
        }
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(val binding: ListItemSelectedUsersBinding) : RecyclerView.ViewHolder(binding.root) {

    }
}