package com.dbottillo.notionalert.sharing

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.dbottillo.notionalert.databinding.ActivitySharingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShareLinkActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySharingBinding

    private val viewModel: SharingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySharingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        lifecycleScope.launch {
            viewModel.events.consumeEach {
                if (it) finish()
            }
        }

        val url = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (intent.action.equals(Intent.ACTION_SEND) && url != null) {
            val title = intent.getStringExtra(Intent.EXTRA_SUBJECT)
            binding.sharingUrl.text = url
            binding.sharingTitle.text = title

            binding.sharingSaveArticle.setOnClickListener {
                viewModel.saveArticle(url = url, title = title)
            }

            binding.sharingSaveLifeOs.setOnClickListener {
                viewModel.saveLifeOs(url = url, title = title)
            }
        } else {
            finish()
        }
    }
}
