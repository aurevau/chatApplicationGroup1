package com.example.chatapplication.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapplication.R
import com.example.chatapplication.data.User

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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_friends, parent, false)
        return UserViewHolder(view)
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

    override fun onBindViewHolder(
        holder: FriendRecyclerAdapter.UserViewHolder,
        position: Int
    ) {
        val friend = friends[position]

        holder.deleteFriend.visibility = View.VISIBLE

        holder.addFriend.setOnClickListener {
            onAddFriendClick(friend)





        }

        holder.deleteFriend.setOnClickListener {
            onDeleteFriendClick(friend)



        }

        holder.itemView.setOnClickListener {
            onItemClick(friend)

        }

        holder.initialCircle.setOnClickListener {
            onProfileClick(friend)
        }




        holder.initialCircle.text = friend.initials.ifBlank { "?" }
        holder.name.text = friend.fullName


    }

    override fun getItemCount(): Int = friends.size

    inner class UserViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val deleteFriend: TextView = itemView.findViewById(R.id.tv_delete_friend_friend)
        val addFriend: TextView = itemView.findViewById(R.id.tv_add_friend_friend)

        val initialCircle: TextView = itemView.findViewById(R.id.tv_initials_friend)
        val name: TextView = itemView.findViewById(R.id.tv_name_friend)

    }


}