package com.example.chatapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapplication.data.Message
import com.example.chatapplication.databinding.ItemMessageReceivedBinding
import com.example.chatapplication.databinding.ItemMessageSentBinding
import com.example.chatapplication.util.DateUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class ChatRecyclerAdapter :
    ListAdapter<Message, RecyclerView.ViewHolder>(Diff()) {

    companion object {
        private const val SENT = 1
        private const val RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        val myId = Firebase.auth.currentUser?.uid
        return if (getItem(position).senderId == myId) SENT else RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == SENT) {
            val binding = ItemMessageSentBinding.inflate(inflater, parent, false)
            SentViewHolder(binding)
        } else {
            val binding = ItemMessageReceivedBinding.inflate(inflater, parent, false)
            ReceivedViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is SentViewHolder -> holder.bind(message)
            is ReceivedViewHolder -> holder.bind(message)
        }
    }

    class SentViewHolder(
        private val binding: ItemMessageSentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {


            if (!message.imageUrl.isNullOrEmpty()) {
                binding.imgMessage.visibility = View.VISIBLE
                Glide.with(binding.imgMessage.context)
                    .load(message.imageUrl)
                    .into(binding.imgMessage)
            } else {
                binding.imgMessage.visibility = View.GONE
            }

            if (!message.text.isNullOrEmpty()) {
                binding.tvMessage.visibility = View.VISIBLE
                binding.tvMessage.text = message.text
            } else {
                binding.tvMessage.visibility = View.GONE
            }
            binding.tvTimestamp.text = DateUtils.formatTimestamp(message.timestamp)
        }

    }

    class ReceivedViewHolder(
        private val binding: ItemMessageReceivedBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {


            if (!message.imageUrl.isNullOrEmpty()) {
                binding.imgMessage.visibility = View.VISIBLE
                Glide.with(binding.imgMessage.context)
                    .load(message.imageUrl)
                    .into(binding.imgMessage)
            } else {
                binding.imgMessage.visibility = View.GONE
            }

            if (!message.text.isNullOrEmpty()) {
                binding.tvMessage.visibility = View.VISIBLE
                binding.tvMessage.text = message.text
            } else {
                binding.tvMessage.visibility = View.GONE
            }
            binding.tvTimestamp.text = DateUtils.formatTimestamp(message.timestamp)
        }

    }

    class Diff : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(a: Message, b: Message) = a.id == b.id
        override fun areContentsTheSame(a: Message, b: Message) = a == b
    }
}