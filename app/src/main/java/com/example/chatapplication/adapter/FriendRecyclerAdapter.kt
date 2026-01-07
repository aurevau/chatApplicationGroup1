package com.example.chatapplication.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapplication.R
import com.example.chatapplication.data.User
import com.example.chatapplication.repository.UserRepository

class FriendRecyclerAdapter(
    val onItemClick: (User) -> Unit,
    val onDeleteFriendClick: (User) -> Unit,
    val onProfileClick: (User) -> Unit,
    val onAcceptFriendRequest: (User) -> Unit,
    val onDeclineFriendRequest: (User) -> Unit
): RecyclerView.Adapter<FriendRecyclerAdapter.UserViewHolder>() {
    private var friendRequests = emptyList<User>()


    private val db = UserRepository()
    private var incomingFriendRequests = emptyList<User>()
    private var combined = emptyList<User>()


    var incomingRequests = mutableSetOf<String>()
    var outgoingRequests = mutableSetOf<String>()

   private var friends = emptyList<User>()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FriendRecyclerAdapter.UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_friends, parent, false)
        return UserViewHolder(view)
    }

    fun updateFriends(list: List<User>) {
        friends = list
        rebuild()
    }

    fun updateIncomingRequests(list: List<User>) {
        incomingFriendRequests = list
        rebuild()
    }

    private fun rebuild() {
        combined = incomingFriendRequests + friends
        notifyDataSetChanged()
    }





    fun updateFriendRequestStatus(
        incoming: Set<String>,
        outgoing: Set<String>
    ) {
        incomingRequests.clear()
        incomingRequests.addAll(incoming)

        outgoingRequests.clear()
        outgoingRequests.addAll(outgoing)

        notifyDataSetChanged()
    }

    override fun onBindViewHolder(
        holder: FriendRecyclerAdapter.UserViewHolder,
        position: Int
    ) {
        val user = combined[position]

        val hasIncomingRequest = incomingRequests.contains(user.id)
        val hasOutgoingRequest = outgoingRequests.contains(user.id)
        val isFriend = friends.any { it.id == user.id }

        holder.deleteFriend.visibility = View.INVISIBLE
        holder.acceptFriend.visibility = View.GONE
        holder.declineFriend.visibility = View.GONE

        when {
            isFriend -> {
                holder.deleteFriend.visibility = View.VISIBLE
                holder.deleteFriend.text = "Friends"
                holder.deleteFriend.setOnClickListener { onDeleteFriendClick(user) }

            }
            hasIncomingRequest -> {
                holder.deleteFriend.visibility = View.INVISIBLE
                holder.acceptFriend.visibility = View.VISIBLE
                holder.acceptFriend.text = "Accept"
                holder.declineFriend.visibility = View.VISIBLE
                holder.declineFriend.text = "Decline"
                holder.declineFriend.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.decline_red)
                )

                holder.acceptFriend.setOnClickListener { onAcceptFriendRequest(user) }
                holder.declineFriend.setOnClickListener { onDeclineFriendRequest(user) }
            }

            else -> {
                holder.deleteFriend.visibility = View.INVISIBLE
                holder.acceptFriend.visibility = View.GONE
                holder.declineFriend.visibility = View.GONE
            }
        }





        holder.deleteFriend.setOnClickListener {
            onDeleteFriendClick(user)

        }

        holder.itemView.setOnClickListener {
            onItemClick(user)

        }

        holder.initialCircle.setOnClickListener {
            onProfileClick(user)
        }

        holder.profilePic.setOnClickListener {
            onProfileClick(user)
        }

        db.getUserDetailsById(user.id ?: "") {
            if (!it?.profileImageUrl.isNullOrEmpty()) {
                holder.initialCircle.visibility = View.INVISIBLE
                holder.profilePic.visibility = View.VISIBLE
                Glide.with(holder.profilePic.context)
                    .load(it.profileImageUrl)
                    .circleCrop()
                    .into(holder.profilePic)
            } else {
                holder.initialCircle.visibility = View.VISIBLE
                holder.profilePic.visibility = View.GONE
                holder.initialCircle.text = user.initials.ifBlank { "?" }
            }
        }


        holder.name.text = user.fullName

    }

    override fun getItemCount(): Int = combined.size

    inner class UserViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val profilePic: ImageView = itemView.findViewById(R.id.profilePic)
        val deleteFriend: TextView = itemView.findViewById(R.id.tv_delete_friend_friend)
        val acceptFriend: TextView = itemView.findViewById(R.id.tv_accept_friend_friend)

        val declineFriend: TextView = itemView.findViewById(R.id.tv_decline_friend_friend)

        val initialCircle: TextView = itemView.findViewById(R.id.tv_initials_friend)
        val name: TextView = itemView.findViewById(R.id.tv_name_friend)

    }


}