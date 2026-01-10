package com.example.chatapplication.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
        val imageUrl = user.profileImageUrl
        if(!imageUrl.isNullOrEmpty()) {
            holder.initials.visibility = View.GONE
            holder.profilePic.visibility = View.VISIBLE
            Glide.with(holder.profilePic.context)
                .load(imageUrl)
                .circleCrop()
                .into(holder.profilePic)

        } else {

            db.getUserDetailsById(user.id ?: "") {

                if (!it?.profileImageUrl.isNullOrEmpty()) {
                    holder.initials.visibility = View.INVISIBLE
                    holder.profilePic.visibility = View.VISIBLE
                    Glide.with(holder.profilePic.context)
                        .load(it.profileImageUrl)
                        .circleCrop()
                        .into(holder.profilePic)
                } else {
                    holder.initials.visibility = View.VISIBLE
                    holder.profilePic.visibility = View.GONE
                    holder.initials.text = user.initials
                }

            }
        }
        holder.name.text = user.fullName
//        holder.initials.text = user.initials
        Log.d("!!!", user.toString())

        holder.itemView.setOnClickListener {
            onItemClick(user)
        }
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tv_name_selected_users)
        val profilePic: ImageView = itemView.findViewById(R.id.profilePic)
        val initials: TextView = itemView.findViewById(R.id.tv_initials_selected_users)

    }
}