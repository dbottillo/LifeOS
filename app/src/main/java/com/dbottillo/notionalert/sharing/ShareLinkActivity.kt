package com.dbottillo.notionalert.sharing

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShareLinkActivity : AppCompatActivity() {

    private val viewModel: SharingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            viewModel.events.consumeEach {
                if (it) finish()
            }
        }

        val url = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (intent.action.equals(Intent.ACTION_SEND) && url != null) {
            val title = intent.getStringExtra(Intent.EXTRA_SUBJECT)
            TODO("something with $title")
            /*binding.sharingUrl.text = url
            binding.sharingTitle.text = title

            binding.sharingSaveArticle.setOnClickListener {
                viewModel.saveArticle(url = url, title = title)
            }

            binding.sharingSaveLifeOs.setOnClickListener {
                viewModel.saveLifeOs(url = url, title = title)
            }*/
        } else {
            finish()
        }
    }
}
