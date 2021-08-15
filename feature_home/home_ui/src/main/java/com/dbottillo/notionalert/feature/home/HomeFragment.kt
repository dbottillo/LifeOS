package com.dbottillo.notionalert.feature.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dbottillo.notionalert.feature.home.databinding.FragmentHomeBinding
import com.dbottillo.notionalert.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: HomeViewModel by viewModels()
    private val binding by viewBinding(FragmentHomeBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonStart.setOnClickListener {
            createNotification()
        }
        binding.buttonStop.setOnClickListener {
            viewModel.removeNotification()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect {
                    render(it)
                }
            }
        }
    }

    private fun createNotification() {
        viewModel.load()
    }

    private fun render(state: AppState) {
        binding.log.text = when (state) {
            is AppState.Idle -> "Idle"
            is AppState.Loading -> "Loading"
            is AppState.Loaded -> "Success, last try: ${state.timestamp}"
            is AppState.Error -> "Error ${state.message}, last try: ${state.timestamp}"
        }
    }
}
