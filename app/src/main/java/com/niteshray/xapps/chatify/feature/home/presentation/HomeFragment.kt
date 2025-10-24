package com.niteshray.xapps.chatify.feature.home.presentation

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.niteshray.xapps.chatify.R
import com.niteshray.xapps.chatify.databinding.DialogFriendRequestsBinding
import com.niteshray.xapps.chatify.databinding.FragmentHomeBinding
import com.niteshray.xapps.chatify.databinding.LayoutNotificationBadgeBinding
import com.niteshray.xapps.chatify.feature.auth.domain.model.User
import com.niteshray.xapps.chatify.feature.home.domain.model.RequestStatus
import com.niteshray.xapps.chatify.feature.home.presentation.adapter.ConnectedFriendsAdapter
import com.niteshray.xapps.chatify.feature.home.presentation.adapter.FriendRequestsAdapter

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private var searchedUser: User? = null
    private var currentUserId: String? = null
    
    private lateinit var connectedFriendsAdapter: ConnectedFriendsAdapter
    private var friendRequestsDialog: Dialog? = null
    private var badgeBinding: LayoutNotificationBadgeBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        
        setupBackPressHandler()
        setupToolbar()
        setupSearchListeners()
        setupConnectedFriendsList()
        observeStates()
        
        // Load connected friends on start
        currentUserId?.let { viewModel.loadConnectedFriends(it) }
        currentUserId?.let { viewModel.loadFriendRequests(it) }
    }

    private fun setupBackPressHandler() {
        // Handle back press to exit app instead of going to login
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Exit the app
                requireActivity().finish()
            }
        })
    }

    private fun setupToolbar() {
        // Setup notification badge
        val notificationItem = binding.toolbar.menu.findItem(R.id.action_notifications)
        badgeBinding = LayoutNotificationBadgeBinding.inflate(layoutInflater)
        notificationItem.actionView = badgeBinding?.root
        
        badgeBinding?.root?.setOnClickListener {
            showFriendRequestsDialog()
        }
        
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    logout()
                    true
                }
                else -> false
            }
        }
    }
    
    private fun updateNotificationBadge(count: Int) {
        badgeBinding?.apply {
            if (count > 0) {
                cvBadge.visibility = View.VISIBLE
                tvBadgeCount.text = if (count > 99) "99+" else count.toString()
            } else {
                cvBadge.visibility = View.GONE
            }
        }
    }

    private fun setupSearchListeners() {
        binding.btnSearch.setOnClickListener {
            searchUser()
        }

        binding.etUsername.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchUser()
                true
            } else {
                false
            }
        }
        
        // Clear search when text changes
        binding.etUsername.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && binding.etUsername.text.toString().trim().isEmpty()) {
                searchUser() // This will show normal screen
            }
        }

        binding.btnSendRequest.setOnClickListener {
            sendFriendRequest()
        }
    }

    private fun setupConnectedFriendsList() {
        connectedFriendsAdapter = ConnectedFriendsAdapter { user ->
            // Navigate to chat with connected friend
            val action = HomeFragmentDirections.actionHomeFragmentToChatFragment(
                userId = user.uid,
                userName = user.name
            )
            findNavController().navigate(action)
        }

        binding.rvConnectedFriends.apply {
            adapter = connectedFriendsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun searchUser() {
        val username = binding.etUsername.text.toString().trim()
        
        if (username.isEmpty()) {
            // Show normal screen when search is empty
            binding.cardResult.visibility = View.GONE
            binding.tvStatus.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
            binding.layoutInitial.visibility = View.VISIBLE
            searchedUser = null
            return
        }

        // Hide result card and show loading
        binding.cardResult.visibility = View.GONE
        binding.tvStatus.visibility = View.GONE
        viewModel.searchUser(username)
    }

    private fun sendFriendRequest() {
        val user = searchedUser ?: return
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        // Get current user data from Firestore to get username
        currentUserId?.let { userId ->
            viewModel.getCurrentUserData(userId) { currentUserData ->
                viewModel.sendFriendRequest(
                    fromUserId = currentUser.uid,
                    toUserId = user.uid,
                    fromUsername = currentUserData?.username ?: "",
                    fromUserName = currentUserData?.name ?: currentUser.email ?: ""
                )
            }
        }
    }

    private fun showFriendRequestsDialog() {
        val dialogBinding = DialogFriendRequestsBinding.inflate(layoutInflater)
        
        friendRequestsDialog = Dialog(requireContext()).apply {
            setContentView(dialogBinding.root)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val requestsAdapter = FriendRequestsAdapter(
            onAcceptClick = { request ->
                currentUserId?.let { userId ->
                    viewModel.acceptFriendRequest(request.id, userId, request.fromUserId)
                }
            },
            onDeclineClick = { request ->
                currentUserId?.let { userId ->
                    viewModel.declineFriendRequest(request.id, userId)
                }
            }
        )

        dialogBinding.rvFriendRequests.apply {
            adapter = requestsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // Observe friend requests
        viewModel.friendRequestsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FriendRequestsState.Success -> {
                    val requestCount = state.requests.size
                    updateNotificationBadge(requestCount)
                    
                    if (requestCount == 0) {
                        dialogBinding.tvNoRequests.visibility = View.VISIBLE
                        dialogBinding.tvNoRequests.text = "No friend requests"
                        dialogBinding.rvFriendRequests.visibility = View.GONE
                    } else {
                        dialogBinding.tvNoRequests.visibility = View.GONE
                        dialogBinding.rvFriendRequests.visibility = View.VISIBLE
                        requestsAdapter.submitList(state.requests)
                    }
                }
                is FriendRequestsState.Error -> {
                    dialogBinding.tvNoRequests.visibility = View.VISIBLE
                    dialogBinding.tvNoRequests.text = "Error loading requests"
                    dialogBinding.rvFriendRequests.visibility = View.GONE
                    
                    Toast.makeText(
                        context, 
                        "Error: ${state.message}", 
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        dialogBinding.ivClose.setOnClickListener {
            friendRequestsDialog?.dismiss()
        }

        friendRequestsDialog?.show()
    }

    private fun observeStates() {
        observeSearchResult()
        observeConnectedFriends()
        observeRequestActions()
        observeFriendshipStatus()
        observeFriendRequests()
    }
    
    private fun observeFriendRequests() {
        viewModel.friendRequestsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FriendRequestsState.Success -> {
                    updateNotificationBadge(state.requests.size)
                }
                is FriendRequestsState.Error -> {
                    // Show error but don't hide the badge - set to 0
                    updateNotificationBadge(0)
                    Toast.makeText(
                        context, 
                        "Failed to load friend requests: ${state.message}", 
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun observeSearchResult() {
        viewModel.searchState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SearchState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.cardResult.visibility = View.GONE
                    binding.layoutInitial.visibility = View.GONE
                }
                is SearchState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    
                    if (state.user != null) {
                        searchedUser = state.user
                        binding.cardResult.visibility = View.VISIBLE
                        binding.layoutInitial.visibility = View.GONE
                        binding.tvUserName.text = state.user.name
                        binding.tvUsername.text = "@${state.user.username}"
                        
                        // Check friendship status
                        currentUserId?.let { 
                            viewModel.checkFriendshipStatus(it, state.user.uid)
                        }
                    } else {
                        binding.cardResult.visibility = View.GONE
                        binding.layoutInitial.visibility = View.VISIBLE
                        Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }
                is SearchState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.cardResult.visibility = View.GONE
                    binding.layoutInitial.visibility = View.VISIBLE
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
                is SearchState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    binding.layoutInitial.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun observeConnectedFriends() {
        viewModel.connectedFriendsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ConnectedFriendsState.Loading -> {
                    binding.tvFriendsTitle.visibility = View.VISIBLE
                    binding.layoutFriendsLoading.visibility = View.VISIBLE
                    binding.rvConnectedFriends.visibility = View.GONE
                }
                is ConnectedFriendsState.Success -> {
                    binding.layoutFriendsLoading.visibility = View.GONE
                    
                    if (state.friends.isEmpty()) {
                        binding.tvFriendsTitle.visibility = View.GONE
                        binding.rvConnectedFriends.visibility = View.GONE
                    } else {
                        binding.tvFriendsTitle.visibility = View.VISIBLE
                        binding.rvConnectedFriends.visibility = View.VISIBLE
                        connectedFriendsAdapter.submitList(state.friends)
                    }
                }
                is ConnectedFriendsState.Error -> {
                    binding.layoutFriendsLoading.visibility = View.GONE
                    binding.tvFriendsTitle.visibility = View.GONE
                    binding.rvConnectedFriends.visibility = View.GONE
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeRequestActions() {
        viewModel.requestActionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RequestActionState.RequestSent -> {
                    Toast.makeText(context, "Friend request sent!", Toast.LENGTH_SHORT).show()
                    binding.btnSendRequest.visibility = View.GONE
                    binding.tvStatus.visibility = View.VISIBLE
                    binding.tvStatus.text = "Request sent"
                }
                is RequestActionState.RequestAccepted -> {
                    Toast.makeText(context, "Friend request accepted!", Toast.LENGTH_SHORT).show()
                    currentUserId?.let { 
                        viewModel.loadFriendRequests(it)
                        viewModel.loadConnectedFriends(it)
                    }
                    friendRequestsDialog?.dismiss()
                }
                is RequestActionState.RequestDeclined -> {
                    Toast.makeText(context, "Request declined", Toast.LENGTH_SHORT).show()
                    currentUserId?.let { viewModel.loadFriendRequests(it) }
                }
                is RequestActionState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeFriendshipStatus() {
        viewModel.friendshipStatusState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FriendshipStatusState.Status -> {
                    when (state.status) {
                        RequestStatus.ACCEPTED -> {
                            // Already friends - hide send request, show status
                            binding.btnSendRequest.visibility = View.GONE
                            binding.tvStatus.visibility = View.VISIBLE
                            binding.tvStatus.text = "Already connected"
                        }
                        RequestStatus.PENDING -> {
                            // Request already sent
                            binding.btnSendRequest.visibility = View.GONE
                            binding.tvStatus.visibility = View.VISIBLE
                            binding.tvStatus.text = "Request pending"
                        }
                        null -> {
                            // No connection - show send request button
                            binding.btnSendRequest.visibility = View.VISIBLE
                            binding.tvStatus.visibility = View.GONE
                        }
                        else -> {
                            binding.btnSendRequest.visibility = View.VISIBLE
                            binding.tvStatus.visibility = View.GONE
                        }
                    }
                }
                is FriendshipStatusState.Error -> {
                    // Show button by default on error
                    binding.btnSendRequest.visibility = View.VISIBLE
                    binding.tvStatus.visibility = View.GONE
                }
            }
        }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        friendRequestsDialog?.dismiss()
        _binding = null
    }
}
