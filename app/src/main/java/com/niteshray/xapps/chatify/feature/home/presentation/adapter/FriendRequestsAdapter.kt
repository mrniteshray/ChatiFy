package com.niteshray.xapps.chatify.feature.home.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.niteshray.xapps.chatify.databinding.ItemFriendRequestBinding
import com.niteshray.xapps.chatify.feature.home.domain.model.FriendRequest

class FriendRequestsAdapter(
    private val onAcceptClick: (FriendRequest) -> Unit,
    private val onDeclineClick: (FriendRequest) -> Unit
) : ListAdapter<FriendRequest, FriendRequestsAdapter.RequestViewHolder>(RequestDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemFriendRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RequestViewHolder(
        private val binding: ItemFriendRequestBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(request: FriendRequest) {
            binding.tvUserName.text = request.fromUserName
            binding.tvUsername.text = "@${request.fromUsername}"

            binding.btnAccept.setOnClickListener {
                onAcceptClick(request)
            }

            binding.btnDecline.setOnClickListener {
                onDeclineClick(request)
            }
        }
    }

    private class RequestDiffCallback : DiffUtil.ItemCallback<FriendRequest>() {
        override fun areItemsTheSame(oldItem: FriendRequest, newItem: FriendRequest): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FriendRequest, newItem: FriendRequest): Boolean {
            return oldItem == newItem
        }
    }
}
