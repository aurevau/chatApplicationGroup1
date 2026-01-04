package com.example.chatapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapplication.data.ChatRoom
import com.example.chatapplication.R

class RecentChatsRecyclerAdapter :
    RecyclerView.Adapter<RecentChatsRecyclerAdapter.ChatViewHolder>() {

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
        holder.bind(chats[position])
    }

    // ViewHolder = håller widgets i varje rad
    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val initials = itemView.findViewById<TextView>(R.id.tvProfileInitials)
        private val name = itemView.findViewById<TextView>(R.id.tvChatName)
        private val message = itemView.findViewById<TextView>(R.id.tvLastMessage)
        private val time = itemView.findViewById<TextView>(R.id.tvTimestamp)

        fun bind(chat: ChatRoom) {
            // Fyll i data här – anpassa efter er ChatRoom-modell
            initials.text = "AB"  // T.ex. första bokstäverna i namnet
            name.text = chat.userName ?: "Okänd"
            message.text = chat.lastMessage ?: "Inget meddelande"
            time.text = chat.timestamp ?: "Nu"
        }
    }
}