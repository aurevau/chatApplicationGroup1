package com.example.chatapplication.adapter

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
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
    val onCancelOutgoingRequest: (User) -> Unit,
    val onAcceptFriendRequest: (User) -> Unit,
    val onDeclineFriendRequest: (User) -> Unit
) : RecyclerView.Adapter<UserRecyclerAdapter.UserViewHolder>() {

    private var users = emptyList<User>()
    private val db = UserRepository()


    private var friends = emptyList<User>()



    private var selection = emptyList<User>()

    private val selectedUsersSet = mutableSetOf<User>()

    var incomingRequests = mutableSetOf<String>()
    var outgoingRequests = mutableSetOf<String>()




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
        val currentUserId = viewModel.getCurrentUserId()

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onCheckButtonClick(user, isChecked)
        }

        val isFriend = friends.any { it.id == user.id }
        val hasIncomingRequest = incomingRequests.contains(user.id)
        val hasOutgoingRequest = outgoingRequests.contains(user.id)

        holder.addFriend.visibility = View.INVISIBLE
        holder.deleteFriend.visibility = View.GONE
        holder.declineFriend.visibility = View.GONE
        holder.cancelFriend.visibility = View.GONE
        holder.acceptFriend.visibility = View.GONE



        holder.addFriend.setOnClickListener(null)
        holder.deleteFriend.setOnClickListener(null)
        holder.declineFriend.setOnClickListener(null)
        holder.cancelFriend.setOnClickListener(null)
        holder.acceptFriend.setOnClickListener(null)



        when {
            isFriend -> {
                holder.addFriend.visibility = View.INVISIBLE
                holder.deleteFriend.visibility = View.VISIBLE
                holder.deleteFriend.text = holder.itemView.context.getString(R.string.friends)
                holder.deleteFriend.setOnClickListener { onDeleteFriendClick(user) }
            }

            hasIncomingRequest -> {
                holder.acceptFriend.visibility = View.VISIBLE
                holder.acceptFriend.text = holder.itemView.context.getString(R.string.accept_btn_text)
                holder.declineFriend.visibility = View.VISIBLE
                holder.declineFriend.text = holder.itemView.context.getString(R.string.decline_friend_request_btn_text)

                holder.acceptFriend.setOnClickListener { onAcceptFriendRequest(user) }
                holder.declineFriend.setOnClickListener { onDeclineFriendRequest(user) }
            }

            hasOutgoingRequest -> {
                holder.addFriend.visibility = View.VISIBLE
                holder.addFriend.text = holder.itemView.context.getString(R.string.pending_btn_text)
                holder.cancelFriend.visibility = View.VISIBLE
                holder.cancelFriend.text = holder.itemView.context.getString(R.string.cancel_alert_btn_text)
                holder.cancelFriend.setOnClickListener { onCancelOutgoingRequest(user) }
            }

            else -> {
                if (user.id == currentUserId) {
                    holder.checkBox.visibility = View.GONE
                    holder.addFriend.visibility = View.INVISIBLE
                }

                holder.addFriend.text = holder.itemView.context.getString(R.string.add_friend)
                holder.deleteFriend.visibility = View.GONE
                holder.acceptFriend.visibility = View.GONE
                holder.declineFriend.visibility = View.GONE
                holder.cancelFriend.visibility = View.GONE
                holder.addFriend.setOnClickListener { onAddFriendClick(user) }
            }
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
                    holder.initialCircle.text = user.initials
                }

            }
        }

        holder.name.text = if (user.id == db.getCurrentUserId()) {
            holder.itemView.context.getString(R.string.me_following_text, user.fullName)
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

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val profilePic: ImageView = itemView.findViewById(R.id.profilePic)
        val deleteFriend: TextView = itemView.findViewById(R.id.tv_delete_friend)
        val addFriend: TextView = itemView.findViewById(R.id.tv_add_friend)
        val acceptFriend: TextView = itemView.findViewById(R.id.tv_accept_friend)

        val declineFriend: TextView = itemView.findViewById(R.id.tv_decline_friend)

        val cancelFriend: TextView = itemView.findViewById(R.id.tv_cancel_friend)
        val button: Button = itemView.findViewById(R.id.btn_start_chat)
        val initialCircle: TextView = itemView.findViewById(R.id.tv_initials)
        val name: TextView = itemView.findViewById(R.id.tv_name)

        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
    }
}