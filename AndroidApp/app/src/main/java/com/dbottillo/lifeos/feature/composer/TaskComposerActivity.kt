package com.dbottillo.lifeos.feature.composer

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dbottillo.lifeos.ui.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskComposerActivity : AppCompatActivity() {

    private val viewModel: TaskComposerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        viewModel.init(
            ComposerInput(
                entryId = intent.getStringExtra(EXTRA_ENTRY_ID),
                url = intent.getStringExtra(Intent.EXTRA_TEXT),
                title = intent.getStringExtra(Intent.EXTRA_SUBJECT),
            )
        )

        setContent {
            AppTheme {
                TaskComposerScreen(
                    navController = null,
                    viewModel = viewModel,
                    close = { finish() }
                )
            }
        }
    }
}

const val EXTRA_ENTRY_ID = "entryId"
