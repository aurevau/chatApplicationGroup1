package com.example.chatapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapplication.data.ChatRoom
import com.example.chatapplication.R

class RecentChatsRecyclerAdapter(
    private val onChatClick: (ChatRoom) -> Unit,
    val onChatLongClick: (ChatRoom) -> Unit,
) : RecyclerView.Adapter<RecentChatsRecyclerAdapter.ChatViewHolder>() {

    private var chats = emptyList<ChatRoom>()  // Ersätt ChatRoom med er model-klass

    // Uppdatera listan när data kommer från ViewModel
    fun setChats(newChats: List<ChatRoom>) {
        chats = newChats
        notifyDataSetChanged()  // Uppdatera RecyclerView
    }

    // Hur många rader?
    override fun getItemCount() = chats.size

    // Skapa en rad (använd item_recent_chat.xml)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_chat, parent, false)
        return ChatViewHolder(view)
    }

    // Fyll i data i varje rad
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chats[position], onChatClick, onChatLongClick )


    }

    // ViewHolder = håller widgets i varje rad
    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
            // Fyll i data här – anpassa efter er ChatRoom-modell

            if(chat.isGroup) {
                chatIcon.setImageResource(R.drawable.group_icon)
                chatIcon.visibility = View.VISIBLE
                fLChatIcon.visibility = View.VISIBLE

                initials.visibility = View.GONE
            } else {
                chatIcon.visibility = View.GONE
                fLChatIcon.visibility = View.GONE
                initials.visibility = View.VISIBLE
            }
            initials.text = chat.userName?.take(2)  // T.ex. första bokstäverna i namnet
            name.text = chat.userName ?: "Okänd"
            message.text = chat.lastMessage ?: "Inget meddelande"
            time.text = chat.timestamp ?: "Nu"
            itemView.setOnClickListener {
                onChatClick(chat)
            }
        }
    }
}