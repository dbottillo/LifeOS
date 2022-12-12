package com.dbottillo.notionalert.feature.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dbottillo.notionalert.R
import com.dbottillo.notionalert.databinding.FragmentHomeBinding
import com.dbottillo.notionalert.viewBinding
import dagger.hilt.android.AndroidEntryPoint
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
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pocketState.collect {
                    renderPocketState(it)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventChannel.collect {
                    when (it) {
                        is PocketEvents.Error -> binding.pocketLogError.text = it.throwable.localizedMessage
                        is PocketEvents.OpenPocket -> openPocket(it.code)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.tryToAuthorizePocket()
    }

    private fun renderPocketState(pocketState: PocketState) {
        when (pocketState) {
            PocketState.Idle -> {
                binding.pocketLog.text = "Pocket - Idle"
                binding.connectPocket.setOnClickListener {
                    viewModel.connectToPocket()
                }
            }
            is PocketState.Authorized -> {
                binding.pocketLog.text = "Access token ${pocketState.accessToken} - username ${pocketState.userName}"
                binding.connectPocket.setOnClickListener {
                    viewModel.immediateRefresh()
                }
            }
        }
    }

    private fun openPocket(code: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    "https://getpocket.com/auth/authorize?request_token=$code&redirect_uri=pocketapp104794:authorizationFinished"
                )
            )
        )
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
            is AppState.Restored -> "Restored, last try: ${state.timestamp}"
        }
    }
}
