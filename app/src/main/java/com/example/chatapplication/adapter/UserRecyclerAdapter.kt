package com.example.chatapplication.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_user, parent, false)
        return UserViewHolder(view)


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






        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = isSelected
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onCheckButtonClick(user, isChecked)
        }

        val isFriend = friends.any { it.id == user.id }

        holder.addFriend.visibility = if (isFriend) View.GONE else View.VISIBLE
        holder.deleteFriend.visibility = if (isFriend) View.VISIBLE else View.GONE




        holder.addFriend.setOnClickListener {
            onAddFriendClick(user)





        }

        holder.deleteFriend.setOnClickListener {
            onDeleteFriendClick(user)



        }


        val imageUrl = user.profileImageUrl

        if (!imageUrl.isNullOrEmpty()) {
            holder.initialCircle.visibility = View.INVISIBLE
            holder.profilePic.visibility = View.VISIBLE

            Glide.with(holder.profilePic.context)
                .load(imageUrl)
                .circleCrop()
                .into(holder.profilePic)
        } else {
            holder.initialCircle.visibility = View.VISIBLE
            holder.profilePic.visibility = View.GONE
            holder.initialCircle.text = user.initials
        }
        holder.name.text = if (user.id == db.getCurrentUserId()) {
            "${user.fullName} (Me)"
        } else {
            user.fullName
        }


        holder.itemView.setOnLongClickListener {
            onItemLongClick(user)
            true
        }


        holder.itemView.setOnClickListener {
            onItemClick(user)

        }

        holder.button.setOnClickListener {
            onButtonClick(user)
        }
    }


    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val profilePic: ImageView = itemView.findViewById(R.id.profilePic)
        val deleteFriend: TextView = itemView.findViewById(R.id.tv_delete_friend)
        val addFriend: TextView = itemView.findViewById(R.id.tv_add_friend)
        val button: Button = itemView.findViewById(R.id.btn_start_chat)
        val initialCircle: TextView = itemView.findViewById(R.id.tv_initials)
        val name: TextView = itemView.findViewById(R.id.tv_name)

        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
    }
}