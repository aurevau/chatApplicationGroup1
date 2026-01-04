package com.example.chatapplication.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapplication.R
import com.example.chatapplication.data.User
import com.example.chatapplication.repository.UserRepository
import com.example.chatapplication.viewmodel.UserViewModel

class UserRecyclerAdapter(
    private val viewModel: UserViewModel,
    val onItemClick: (User) -> Unit,
    val onButtonClick: (User) -> Unit,
    val onAddFriendClick: (User) -> Unit,
    val onDeleteFriendClick: (User) -> Unit,
    val onCheckButtonClick: (List<User>) -> Unit
): RecyclerView.Adapter<UserRecyclerAdapter.UserViewHolder>() {



    private val selectedUsers = mutableSetOf<User>()

    private var users = emptyList<User>()
    private val db = UserRepository()

    private var friends = emptyList<User>()


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserRecyclerAdapter.UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_user, parent, false)
        return UserViewHolder(view)


    }

    fun updateFriendList(newFriends: List<User>) {
        friends = newFriends
        notifyDataSetChanged()
    }

    fun submitList(userList: List<User>) {
        users = userList
        notifyDataSetChanged()
    }

    fun getSelectedUsers(): List<User> = selectedUsers.toList()

    override fun onBindViewHolder(
        holder: UserRecyclerAdapter.UserViewHolder,
        position: Int
    ) {


        val user = users[position]

        val isFriend = friends.any { it.id == user.id }

        holder.addFriend.visibility = if (isFriend) View.GONE else View.VISIBLE
        holder.addFriend.isEnabled = !isFriend

        holder.deleteFriend.visibility = if (isFriend) View.VISIBLE else View.GONE
        holder.deleteFriend.isEnabled = isFriend

        holder.addFriend.setOnClickListener {
            onAddFriendClick(user) // ändrar Firebase och LiveData
        }

        holder.deleteFriend.setOnClickListener {
            onDeleteFriendClick(user)
        }





        holder.initialCircle.text = user.initials.ifBlank {  "?"}
        holder.name.text = if(user.id == db.getCurrentUserId()) {
            "${user.fullName} (Me)"
        } else {
            user.fullName
        }


        holder.checkBox.isChecked = selectedUsers.contains(user)


        holder.checkBox.setOnClickListener {
            if(holder.checkBox.isChecked) selectedUsers.add(user)
            else selectedUsers.remove(user)
            onCheckButtonClick(selectedUsers.toList())
            Log.d("!!!", "selected users ${selectedUsers.size}")
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
        val deleteFriend:TextView = itemView.findViewById(R.id.tv_delete_friend)
        val addFriend: TextView = itemView.findViewById(R.id.tv_add_friend)
        val button: Button = itemView.findViewById(R.id.btn_start_chat)
        val initialCircle: TextView = itemView.findViewById(R.id.tv_initials)
        val name: TextView = itemView.findViewById(R.id.tv_name)

        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
    }
}