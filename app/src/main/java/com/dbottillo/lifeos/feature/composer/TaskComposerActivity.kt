package com.dbottillo.lifeos.feature.composer

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import com.dbottillo.lifeos.ui.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskComposerActivity : AppCompatActivity() {

    private val viewModel: TaskComposerViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        viewModel.init(
            ComposerInput(
                url = intent.getStringExtra(Intent.EXTRA_TEXT),
                title = intent.getStringExtra(Intent.EXTRA_SUBJECT),
            )
        )

        setContent {
            AppTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Composer") }
                        )
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .consumeWindowInsets(it)
                            .padding(it)
                            .safeDrawingPadding(),
                    ) {
                        TaskComposerScreen(
                            navController = null,
                            viewModel = viewModel,
                            close = { finish() }
                        )
                    }
                }
            }
        }
    }
}
