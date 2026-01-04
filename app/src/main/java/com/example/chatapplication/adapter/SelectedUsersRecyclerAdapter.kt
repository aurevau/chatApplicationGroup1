package com.example.chatapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapplication.R
import com.example.chatapplication.data.User
import com.example.chatapplication.repository.UserRepository

class SelectedUsersRecyclerAdapter: RecyclerView.Adapter<SelectedUsersRecyclerAdapter.UserViewHolder>() {
    val selectedUsers = mutableSetOf<User>()
    private var users = emptyList<User>()
    private val db = UserRepository()



    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SelectedUsersRecyclerAdapter.UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_selected_users, parent, false)
        return UserViewHolder(view)
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
        holder.textViewSelectedUser.text = user.fullName
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val textViewSelectedUser: TextView = itemView.findViewById(R.id.tv_selected_user)

    }
}