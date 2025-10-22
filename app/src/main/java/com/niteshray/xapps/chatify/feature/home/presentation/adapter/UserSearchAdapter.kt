package com.niteshray.xapps.chatify.feature.home.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.niteshray.xapps.chatify.databinding.ItemUserSearchBinding
import com.niteshray.xapps.chatify.feature.auth.domain.model.User

class UserSearchAdapter(
    private val onAddFriendClick: (User) -> Unit
) : ListAdapter<User, UserSearchAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        println("DEBUG Adapter: onCreateViewHolder called")
        val binding = ItemUserSearchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        println("DEBUG Adapter: onBindViewHolder called for position $position, user: ${user.name}")
        holder.bind(user)
    }
    
    override fun submitList(list: List<User>?) {
        println("DEBUG Adapter: submitList called with ${list?.size ?: 0} items")
        list?.forEach { user ->
            println("DEBUG Adapter: Submitting user - ${user.name} (${user.email})")
        }
        super.submitList(list)
    }
    
    override fun getItemCount(): Int {
        val count = super.getItemCount()
        println("DEBUG Adapter: getItemCount returning $count")
        return count
    }

    inner class UserViewHolder(
        private val binding: ItemUserSearchBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.apply {
                tvUserName.text = user.name
                tvUserEmail.text = user.email

                btnAddFriend.setOnClickListener {
                    onAddFriendClick(user)
                }
            }
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}
