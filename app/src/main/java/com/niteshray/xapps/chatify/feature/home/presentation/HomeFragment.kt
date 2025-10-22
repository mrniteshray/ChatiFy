package com.niteshray.xapps.chatify.feature.home.presentation

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.niteshray.xapps.chatify.R
import com.niteshray.xapps.chatify.databinding.FragmentHomeBinding
import com.niteshray.xapps.chatify.feature.home.presentation.adapter.UsersAdapter

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var usersAdapter: UsersAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupToolbar()
        setupRecyclerView()
        observeUsers()
        loadUsers()
    }

    private fun setupToolbar() {
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

    private fun setupRecyclerView() {
        usersAdapter = UsersAdapter { user ->
            // Handle user click - can add chat functionality later
            Toast.makeText(context, "Clicked on ${user.name}", Toast.LENGTH_SHORT).show()
        }

        binding.rvUsers.apply {
            adapter = usersAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeUsers() {
        viewModel.usersState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UsersState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvUsers.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.GONE
                }
                is UsersState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvUsers.visibility = View.VISIBLE
                    binding.tvEmptyState.visibility = View.GONE
                    usersAdapter.submitList(state.users)
                }
                is UsersState.Empty -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvUsers.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.VISIBLE
                }
                is UsersState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun loadUsers() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModel.loadAllUsers(currentUserId)
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
