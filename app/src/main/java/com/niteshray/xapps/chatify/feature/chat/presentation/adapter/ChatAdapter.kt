package com.niteshray.xapps.chatify.feature.chat.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.niteshray.xapps.chatify.databinding.ItemMessageReceivedBinding
import com.niteshray.xapps.chatify.databinding.ItemMessageSentBinding
import com.niteshray.xapps.chatify.feature.chat.domain.model.Message
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val currentUserId: String
) : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val binding = ItemMessageSentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            SentMessageViewHolder(binding)
        } else {
            val binding = ItemMessageReceivedBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            ReceivedMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
        }
    }

    inner class SentMessageViewHolder(
        private val binding: ItemMessageSentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.apply {
                tvMessage.text = message.message
                tvTimestamp.text = formatTime(message.timestamp)
                
                // Show read status
                if (message.isRead) {
                    ivReadStatus.visibility = View.VISIBLE
                } else {
                    ivReadStatus.visibility = View.VISIBLE
                    ivReadStatus.alpha = 0.5f
                }
            }
        }
    }

    inner class ReceivedMessageViewHolder(
        private val binding: ItemMessageReceivedBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.apply {
                tvMessage.text = message.message
                tvTimestamp.text = formatTime(message.timestamp)
            }
        }
    }

    private fun formatTime(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp

        val now = Calendar.getInstance()
        
        return when {
            // Today
            calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) &&
            calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) -> {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
            }
            // Yesterday
            calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) - 1 &&
            calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) -> {
                "Yesterday " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
            }
            // This year
            calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) -> {
                SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(timestamp))
            }
            // Other years
            else -> {
                SimpleDateFormat("MMM dd yyyy, HH:mm", Locale.getDefault()).format(Date(timestamp))
            }
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}
