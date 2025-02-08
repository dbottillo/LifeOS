package com.dbottillo.lifeos.feature.composer

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.dbottillo.lifeos.ui.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.consumeEach

@AndroidEntryPoint
class TaskComposerActivity : AppCompatActivity() {

    private val viewModel: TaskComposerViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        viewModel.init(
            url = intent.getStringExtra(Intent.EXTRA_TEXT),
            title = intent.getStringExtra(Intent.EXTRA_SUBJECT),
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
                    ){
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

@Composable
fun TaskComposerScreen(
    navController: NavHostController? = null,
    viewModel: TaskComposerViewModel,
    close: (() -> Unit)? = null,
){

    LaunchedEffect(Unit) {
        viewModel.events.consumeEach {
            when (it) {
                ComposerEvents.Finish -> {
                    if (navController == null){
                        close?.invoke()
                    } else {
                        navController.popBackStack()
                    }
                }
            }
        }
    }
    val state = viewModel.state.collectAsStateWithLifecycle()
        TaskComposerScreenContent(
            state = state.value,
            onTitleChange = viewModel::onTitleChange,
            onUrlChange = viewModel::onUrlChange,
            saveArticle = viewModel::saveArticle,
            saveLifeOs = viewModel::saveLifeOs,
            onTypeSelected = viewModel::onTypeSelected,
            onStatusSelected = viewModel::onStatusSelected,
            onSelectDate = viewModel::onSelectDate,
            onDateSelected = viewModel::onDateSelected,
            onDateSelectionDismiss = viewModel::onDateSelectionDismiss
        )
}

@Composable
private fun TaskComposerScreenContent(
    state: ComposerState,
    onTitleChange: (String) -> Unit = {},
    onUrlChange: (String) -> Unit = {},
    saveArticle: () -> Unit,
    saveLifeOs: () -> Unit,
    onTypeSelected: (String) -> Unit,
    onStatusSelected: (String) -> Unit,
    onSelectDate: () -> Unit,
    onDateSelected: (Long?) -> Unit,
    onDateSelectionDismiss: () -> Unit
) {
    Box {
        if (state.showDueDatePicker) {
            DatePickerModal(
                onDateSelected = onDateSelected,
                onDismiss = onDateSelectionDismiss
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()

        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.title,
                    onValueChange = onTitleChange,
                    label = { Text("Title") }
                )
                Column {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.sanitizedUrl,
                        onValueChange = onUrlChange,
                        label = { Text("Url") }
                    )
                    if (state.url != state.sanitizedUrl) {
                        Text(
                            text = "Original url: ${state.url}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
                Selector(
                    prefix = "Type",
                    selection = state.typeSelection,
                    options = state.typeSelectorOptions,
                    onOptionSelected = onTypeSelected
                )
                Selector(
                    prefix = "Status",
                    selection = state.statusSelection,
                    options = state.statusSelectorOptions,
                    onOptionSelected = onStatusSelected
                )
                DueDatePicker(
                    dueDate = state.formattedDate,
                    onSelectDate = onSelectDate
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    modifier = Modifier.padding(top = 24.dp),
                    onClick = { saveLifeOs() }
                ) {
                    Text(text = "Task")
                }
                Button(
                    modifier = Modifier.padding(top = 24.dp),
                    onClick = { saveArticle() },
                    enabled = state.saveArticleEnabled
                ) {
                    Text(text = "Article")
                }
            }
        }
    }
}

@Composable
fun Selector(
    prefix: String,
    selection: String?,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier.padding(end = 16.dp),
            text = "$prefix: $selection",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary,
        )
        Box {
            var expanded by remember { mutableStateOf(false) }
            Button(onClick = { expanded = !expanded }) {
                Text("Select")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DueDatePicker(
    dueDate: String?,
    onSelectDate: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier.padding(end = 16.dp),
            text = "Due: $dueDate",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary,
        )
        Button(onClick = onSelectDate) {
            Text("Select")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Preview
@Composable
fun ShareScreenPreview() {
    AppTheme {
        Box(
            modifier = Modifier.background(Color.White)
        ){
            TaskComposerScreenContent(
                state = ComposerState(
                    url = "https://www.google.com?abc=def",
                    title = "Google",
                    typeSelectorOptions = listOf(
                        "Idea",
                        "Task",
                        "Resource",
                        "Project",
                        "Bookmark",
                        "Area"
                    ),
                    statusSelectorOptions = listOf(
                        "Focus",
                        "Blocked",
                        "Backlog",
                        "Recurring",
                        "Archive",
                        "Done"
                    ),
                ),
                saveArticle = { },
                saveLifeOs = { },
                onTypeSelected = { },
                onStatusSelected = {},
                onDateSelected = { _ -> },
                onDateSelectionDismiss = {},
                onSelectDate = {}
            )
        }
    }
}
