package com.niteshray.xapps.chatify.feature.chat.presentation

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.niteshray.xapps.chatify.R
import com.niteshray.xapps.chatify.databinding.FragmentChatBinding
import com.niteshray.xapps.chatify.feature.chat.presentation.adapter.ChatAdapter

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by viewModels()
    private val args: ChatFragmentArgs by navArgs()
    private lateinit var chatAdapter: ChatAdapter

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChatBinding.bind(view)

        setupToolbar()
        setupRecyclerView()
        setupMessageInput()
        observeMessages()
        observeSendMessageState()
        
        loadMessages()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            title = args.userName
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(currentUserId)

        binding.rvMessages.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(requireContext()).apply {
                // Messages will now show from top to bottom naturally
                reverseLayout = false
                stackFromEnd = false
            }
        }
    }

    private fun setupMessageInput() {
        binding.btnSend.setOnClickListener {
            val messageText = binding.etMessage.text.toString()
            if (messageText.isNotBlank()) {
                sendMessage(messageText)
            }
        }
    }

    private fun observeMessages() {
        viewModel.messagesState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is MessagesState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvMessages.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.GONE
                }
                is MessagesState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvMessages.visibility = View.VISIBLE
                    binding.tvEmptyState.visibility = View.GONE
                    
                    chatAdapter.submitList(state.messages) {
                        // Scroll to bottom when new messages arrive
                        if (state.messages.isNotEmpty()) {
                            binding.rvMessages.scrollToPosition(state.messages.size - 1)
                        }
                    }
                    
                    // Mark received messages as read
                    state.messages
                        .filter { it.receiverId == currentUserId && !it.isRead }
                        .forEach { viewModel.markMessageAsRead(it.id) }
                }
                is MessagesState.Empty -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvMessages.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.VISIBLE
                }
                is MessagesState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun observeSendMessageState() {
        viewModel.sendMessageState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SendMessageState.Loading -> {
                    binding.btnSend.isEnabled = false
                }
                is SendMessageState.Success -> {
                    binding.btnSend.isEnabled = true
                    binding.etMessage.text?.clear()
                    viewModel.resetSendMessageState()
                }
                is SendMessageState.Error -> {
                    binding.btnSend.isEnabled = true
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    viewModel.resetSendMessageState()
                }
                is SendMessageState.Idle -> {
                    binding.btnSend.isEnabled = true
                }
            }
        }
    }

    private fun loadMessages() {
        viewModel.loadMessages(currentUserId, args.userId)
    }

    private fun sendMessage(messageText: String) {
        viewModel.sendMessage(
            senderId = currentUserId,
            receiverId = args.userId,
            messageText = messageText
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
