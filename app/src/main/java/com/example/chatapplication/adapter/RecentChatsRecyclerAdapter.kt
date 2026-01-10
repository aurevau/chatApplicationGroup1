package com.example.chatapplication.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapplication.data.ChatRoom
import com.example.chatapplication.R
import com.example.chatapplication.util.DateUtils

class RecentChatsRecyclerAdapter(
    var currentUserFullName: String,
    private val onChatClick: (ChatRoom) -> Unit,
    val onChatLongClick: (ChatRoom) -> Unit,

    ) : RecyclerView.Adapter<RecentChatsRecyclerAdapter.ChatViewHolder>() {

    private var chats = emptyList<ChatRoom>()

    // Update the list when data comes from the ViewModel
    fun setChats(newChats: List<ChatRoom>) {
        chats = newChats
        notifyDataSetChanged()  // Update RecyclerView
    }


    override fun getItemCount() = chats.size

    // Create a row (use item_recent_chat.xml)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_chat, parent, false)
        return ChatViewHolder(view)
    }

    // Fill in the data in each row
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chats[position], onChatClick, onChatLongClick)
    }

    // ViewHolder = holds widgets in each row
    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val profilePic: ImageView = itemView.findViewById(R.id.profilePic)
        private val initials = itemView.findViewById<TextView>(R.id.tvProfileInitials)
        private val name = itemView.findViewById<TextView>(R.id.tvChatName)
        private val message = itemView.findViewById<TextView>(R.id.tvLastMessage)
        private val time = itemView.findViewById<TextView>(R.id.tvTimestamp)

        private val chatIcon = itemView.findViewById<ImageView>(R.id.ivChatIcon)
        private val fLChatIcon = itemView.findViewById<FrameLayout>(R.id.fl_ChatIcon)

        fun bind(
            chat: ChatRoom,
            onChatClick: (ChatRoom) -> Unit,
            onChatLongClick: (ChatRoom) -> Unit
        ) {

            itemView.setOnLongClickListener {
                onChatLongClick(chat)
                true
            }

            if (chat.isGroup) {
                chatIcon.setImageResource(R.drawable.group_icon)
                chatIcon.visibility = View.VISIBLE
                fLChatIcon.visibility = View.VISIBLE
                profilePic.visibility = View.GONE
                initials.visibility = View.GONE
            } else {
                chatIcon.visibility = View.GONE
                fLChatIcon.visibility = View.GONE
                val imageUrl = chat.chatRoomImageUrl
                if (!imageUrl.isNullOrEmpty()) {
                    initials.visibility = View.GONE
                    profilePic.visibility = View.VISIBLE
                    Glide.with(profilePic.context)
                        .load(imageUrl)
                        .circleCrop()
                        .into(profilePic)
                } else {
                    initials.visibility = View.VISIBLE
                    profilePic.visibility = View.GONE
                    initials.text = chat.userName?.take(2)
                }
            }

            val displayName = when {
                !chat.groupName.isNullOrBlank() -> chat.groupName
                chat.isGroup && !chat.memberNames.isNullOrEmpty() -> buildGroupName(chat.memberNames) // group without groupname
                chat.userName == currentUserFullName -> itemView.context.getString(
                    R.string.me_following_text,
                    currentUserFullName
                ) // Chat with yourself
                else -> chat.userName ?: itemView.context.getString(R.string.unknown)
            }
            name.text = displayName


            message.text = when {
                !chat.lastMessage.isNullOrBlank() -> chat.lastMessage
                !chat.lastImageMessage.isNullOrEmpty() ->
                    itemView.context.getString(R.string.picture_message_text)

                else -> ""
            }


            initials.text = chat.userName?.take(2)  // The two first letters of the name
            time.text = DateUtils.formatTimestamp(chat.timestamp)
            itemView.setOnClickListener {
                onChatClick(chat)
            }
        }

        private fun buildGroupName(allUserNames: List<String?>): String {
            val names = allUserNames
                .filterNotNull()
                .filter { it.isNotBlank() && it != currentUserFullName }
                .map { it.substringBefore(" ") }
            return if (names.isEmpty()) itemView.context.getString(R.string.group) else names.joinToString(
                ", "
            )
        }

    }


}

