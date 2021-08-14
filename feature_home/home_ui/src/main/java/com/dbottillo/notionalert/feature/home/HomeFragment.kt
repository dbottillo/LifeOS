package com.dbottillo.notionalert.feature.home

import android.os.Bundle
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dbottillo.notionalert.CHANNEL_ID
import com.dbottillo.notionalert.MAIN_NOTIFICATION_ID
import com.dbottillo.notionalert.feature.home.databinding.FragmentHomeBinding
import com.dbottillo.notionalert.viewBinding
import dagger.hilt.android.AndroidEntryPoint

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
    }

    private fun createNotification() {
        with(NotificationManagerCompat.from(requireContext())) {
            notify(MAIN_NOTIFICATION_ID, builder.build())
        }
    }

    private fun removeNotification() {
        with(NotificationManagerCompat.from(requireContext())) {
            cancel(MAIN_NOTIFICATION_ID)
        }
    }

    private val builder by lazy {
        NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("NotionAlert")
            .setContentText("Much longer text that cannot fit one line...")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Much longer text that cannot fit one line...")
            )
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    }
}
