package com.example.chatapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapplication.R
import com.example.chatapplication.data.User
import com.example.chatapplication.repository.UserRepository

class SelectedUsersRecyclerAdapter(val onItemClick: (User) -> Unit) :
    RecyclerView.Adapter<SelectedUsersRecyclerAdapter.UserViewHolder>() {

    private var users = listOf<User>()
    private val db = UserRepository()


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SelectedUsersRecyclerAdapter.UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_selected_users, parent, false)
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
        holder.name.text = user.fullName
        holder.initials.text = user.initials

        holder.itemView.setOnClickListener {
            onItemClick(user)
        }
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tv_name_selected_users)
        val initials: TextView = itemView.findViewById(R.id.tv_initials_selected_users)

    }
}