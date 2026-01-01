package com.example.chatapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapplication.R
import com.example.chatapplication.data.User

class UserRecyclerAdapter(val onItemClick: (User) -> Unit): RecyclerView.Adapter<UserRecyclerAdapter.UserViewHolder>() {

    private var users = emptyList<User>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserRecyclerAdapter.UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_user, parent, false)
        return UserViewHolder(view)
    }

    fun submitList(userList: List<User>) {
        users = userList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(
        holder: UserRecyclerAdapter.UserViewHolder,
        position: Int
    ) {
        val user = users[position]
        holder.initialCircle.text = user.initials
        holder.username.text = user.username
        holder.name.text = user.name

        holder.itemView.setOnClickListener {
            onItemClick(user)
        }
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val initialCircle: TextView = itemView.findViewById(R.id.tv_initials)
        val username: TextView = itemView.findViewById(R.id.tvUsername)
        val name: TextView = itemView.findViewById(R.id.tv_name)

    }
}