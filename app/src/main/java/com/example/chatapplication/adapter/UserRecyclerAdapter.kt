package com.example.chatapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapplication.R
import com.example.chatapplication.data.User
import com.example.chatapplication.repository.UserRepository
import com.example.chatapplication.viewmodel.UserViewModel

class UserRecyclerAdapter(val onItemClick: (User) -> Unit,
    val onButtonClick: (User) -> Unit): RecyclerView.Adapter<UserRecyclerAdapter.UserViewHolder>() {

    private var users = emptyList<User>()
    private val db = UserRepository()


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

        if (user.fullName.isBlank()) return
        holder.initialCircle.text = user.initials
        holder.name.text = if(user.id == db.getCurrentUserId()) {
            "${user.fullName} (Me)"
        } else {
            user.fullName
        }

        holder.itemView.setOnClickListener {
            onItemClick(user)
        }

        holder.button.setOnClickListener {
            onButtonClick(user)
        }
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val button: Button = itemView.findViewById(R.id.btn_start_chat)
        val initialCircle: TextView = itemView.findViewById(R.id.tv_initials)
        val name: TextView = itemView.findViewById(R.id.tv_name)

    }
}