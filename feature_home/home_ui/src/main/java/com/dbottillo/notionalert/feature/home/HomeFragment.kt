package com.dbottillo.notionalert.feature.home

import android.os.Bundle
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dbottillo.notionalert.ApiResult
import com.dbottillo.notionalert.CHANNEL_ID
import com.dbottillo.notionalert.MAIN_NOTIFICATION_ID
import com.dbottillo.notionalert.NotionPage
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
            removeNotification()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect {
                    it?.let { render(it) }
                }
            }
        }
    }

    private fun createNotification() {
        with(NotificationManagerCompat.from(requireContext())) {
            notify(
                MAIN_NOTIFICATION_ID,
                getNotificationBuilder("NotionAlert", "Waiting...").build()
            )
        }
        viewModel.load()
    }

    private fun removeNotification() {
        with(NotificationManagerCompat.from(requireContext())) {
            cancel(MAIN_NOTIFICATION_ID)
        }
    }

    private fun getNotificationBuilder(title: String, text: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(text)
            )
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    }

    private fun render(state: ApiResult<NotionPage>) {
        binding.log.text = state.toString()

        if (state is ApiResult.Success) {
            val nameProperty = state.data.properties["Name"]
            val notionTitle = nameProperty?.title?.get(0)
                ?: throw UnsupportedOperationException("notion title is null")
            with(NotificationManagerCompat.from(requireContext())) {
                notify(
                    MAIN_NOTIFICATION_ID,
                    getNotificationBuilder("NotionAlert", notionTitle.plainText).build()
                )
            }
        }
    }
}
