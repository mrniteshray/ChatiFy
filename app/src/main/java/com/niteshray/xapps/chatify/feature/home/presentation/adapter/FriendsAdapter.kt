package com.niteshray.xapps.chatify.feature.home.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.niteshray.xapps.chatify.databinding.ItemFriendBinding
import com.niteshray.xapps.chatify.feature.home.domain.model.Friend
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FriendsAdapter(
    private val onFriendClick: (Friend) -> Unit
) : ListAdapter<Friend, FriendsAdapter.FriendViewHolder>(FriendDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemFriendBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FriendViewHolder(
        private val binding: ItemFriendBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(friend: Friend) {
            binding.apply {
                tvFriendName.text = friend.name
                tvFriendEmail.text = friend.email
                
                // Set online status
                if (friend.isOnline) {
                    tvOnlineStatus.text = "Online"
                } else {
                    tvOnlineStatus.text = formatLastSeen(friend.lastSeen)
                }

                // Handle click
                root.setOnClickListener {
                    onFriendClick(friend)
                }
            }
        }

        private fun formatLastSeen(timestamp: Long): String {
            if (timestamp == 0L) return "Offline"
            
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            
            return when {
                diff < 60000 -> "Just now"
                diff < 3600000 -> "${diff / 60000} min ago"
                diff < 86400000 -> "${diff / 3600000} hr ago"
                diff < 604800000 -> "${diff / 86400000} days ago"
                else -> {
                    val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
                    sdf.format(Date(timestamp))
                }
            }
        }
    }

    class FriendDiffCallback : DiffUtil.ItemCallback<Friend>() {
        override fun areItemsTheSame(oldItem: Friend, newItem: Friend): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: Friend, newItem: Friend): Boolean {
            return oldItem == newItem
        }
    }
}
