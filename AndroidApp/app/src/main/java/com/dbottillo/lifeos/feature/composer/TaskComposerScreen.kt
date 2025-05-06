package com.dbottillo.lifeos.feature.composer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.dbottillo.lifeos.ui.AppTheme
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.job

@Composable
fun TaskComposerScreen(
    navController: NavHostController? = null,
    viewModel: TaskComposerViewModel,
    close: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.events.consumeEach {
            when (it) {
                ComposerEvents.Finish -> {
                    if (navController == null) {
                        close?.invoke()
                    } else {
                        navController.navigateUp()
                    }
                }
                is ComposerEvents.Error ->
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
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
@OptIn(ExperimentalMaterial3Api::class)
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
    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        topBar = {
            TopAppBar(
                title = { Text("Composer") }
            )
        },
        bottomBar = {
            if (state is ComposerState.Data) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    if (state.showArticle) {
                        MainActionButton(
                            modifier = Modifier.weight(0.5f).padding(end = 8.dp),
                            editingInProgress = state.editingInProgress,
                            editTaskMode = state.editTaskMode,
                            saveLifeOs = saveLifeOs
                        )
                        ArticleActionButton(
                            modifier = Modifier.weight(0.5f).padding(start = 8.dp),
                            enabled = state.saveArticleEnabled,
                            saveArticle = saveArticle
                        )
                    } else {
                        MainActionButton(
                            modifier = Modifier.weight(1f),
                            editingInProgress = state.editingInProgress,
                            editTaskMode = state.editTaskMode,
                            saveLifeOs = saveLifeOs
                        )
                    }
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .consumeWindowInsets(it)
                .padding(it)
                .safeDrawingPadding()
        ) {
            TaskComposerScreenDataContent(
                state = state,
                onTitleChange = onTitleChange,
                onUrlChange = onUrlChange,
                onTypeSelected = onTypeSelected,
                onStatusSelected = onStatusSelected,
                onSelectDate = onSelectDate,
                onDateSelected = onDateSelected,
                onDateSelectionDismiss = onDateSelectionDismiss
            )
        }
    }
}

@Composable
private fun MainActionButton(
    modifier: Modifier = Modifier,
    editingInProgress: Boolean,
    editTaskMode: Boolean,
    saveLifeOs: () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = saveLifeOs
    ) {
        if (editingInProgress) {
            Box(
                modifier = Modifier.size(24.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.width(16.dp).align(Alignment.Center),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        } else {
            Text(text = if (editTaskMode) "Edit" else "Task")
        }
    }
}

@Composable
private fun ArticleActionButton(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    saveArticle: () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = saveArticle,
        enabled = enabled
    ) {
        Text(text = "Article")
    }
}

@Composable
fun TaskComposerScreenDialog(
    navController: NavHostController? = null,
    viewModel: TaskComposerViewModel,
    close: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.events.consumeEach {
            when (it) {
                ComposerEvents.Finish -> {
                    if (navController == null) {
                        close?.invoke()
                    } else {
                        navController.navigateUp()
                    }
                }

                is ComposerEvents.Error -> {
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    val state = viewModel.state.collectAsStateWithLifecycle()
    Dialog(onDismissRequest = { navController?.navigateUp() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            TaskComposerScreenContentDialog(
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
    }
}

@Composable
private fun TaskComposerScreenDataContent(
    state: ComposerState,
    onTitleChange: (String) -> Unit = {},
    onUrlChange: (String) -> Unit = {},
    onTypeSelected: (String) -> Unit,
    onStatusSelected: (String) -> Unit,
    onSelectDate: () -> Unit,
    onDateSelected: (Long?) -> Unit,
    onDateSelectionDismiss: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    when (state) {
        ComposerState.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.width(48.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
        is ComposerState.Data -> {
            if (state.showDueDatePicker) {
                DatePickerModal(
                    onDateSelected = onDateSelected,
                    onDismiss = onDateSelectionDismiss
                )
            }
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    value = state.title,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    onValueChange = onTitleChange,
                    label = { Text("Title") }
                )
                Column {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.sanitizedUrl,
                        onValueChange = onUrlChange,
                        label = { Text("Link") }
                    )
                    if (state.link != state.sanitizedUrl) {
                        Text(
                            text = "Original url: ${state.link}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
                Selector(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    prefix = "Type",
                    selection = state.typeSelection,
                    options = state.typeSelectorOptions,
                    onOptionSelected = onTypeSelected
                )
                Selector(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    prefix = "Status",
                    selection = state.statusSelection,
                    options = state.statusSelectorOptions,
                    onOptionSelected = onStatusSelected
                )
                DueDatePicker(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    dueDate = state.formattedDate,
                    onSelectDate = onSelectDate
                )
            }
        }
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun TaskComposerScreenContentDialog(
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
    val focusRequester = remember { FocusRequester() }
    when (state) {
        ComposerState.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.width(48.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
        is ComposerState.Data -> {
            if (state.showDueDatePicker) {
                DatePickerModal(
                    onDateSelected = onDateSelected,
                    onDismiss = onDateSelectionDismiss
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = "Composer",
                    style = MaterialTheme.typography.titleLarge
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
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
                    if (state.link != state.sanitizedUrl) {
                        Text(
                            text = "Original url: ${state.link}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
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
                }
                DueDatePicker(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    dueDate = state.formattedDate,
                    onSelectDate = onSelectDate
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        modifier = Modifier.padding(top = 24.dp),
                        onClick = { saveLifeOs() }
                    ) {
                        Text(text = if (state.editTaskMode) "Edit" else "Task")
                    }
                    if (state.showArticle) {
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
    }
    LaunchedEffect(Unit) {
        this.coroutineContext.job.invokeOnCompletion {
            focusRequester.requestFocus()
        }
    }
}

@Composable
fun Selector(
    modifier: Modifier = Modifier,
    prefix: String,
    selection: String?,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    Row(
        modifier = modifier,
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
    modifier: Modifier = Modifier,
    dueDate: String?,
    onSelectDate: () -> Unit
) {
    Row(
        modifier = modifier,
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
        ) {
            TaskComposerScreenContent(
                state = ComposerState.Data(
                    entryId = null,
                    link = "https://www.google.com?abc=def",
                    title = "Google",
                    typeSelectorOptions = listOf(
                        "Task",
                        "Resource",
                        "Folder",
                        "Bookmark",
                        "Area"
                    ),
                    statusSelectorOptions = listOf(
                        "Focus",
                        "Next week",
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

@Preview
@Composable
fun ShareScreenPreviewLoading() {
    AppTheme {
        Box(
            modifier = Modifier.background(Color.White)
        ) {
            TaskComposerScreenContent(
                state = ComposerState.Loading,
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

@Preview(device = Devices.PIXEL_TABLET, widthDp = 500, heightDp = 500)
@Composable
fun ShareScreenPreviewDialog() {
    AppTheme {
        Box(
            modifier = Modifier.background(Color.White)
        ) {
            TaskComposerScreenContentDialog(
                state = ComposerState.Data(
                    entryId = null,
                    link = "https://www.google.com?abc=def",
                    title = "Google",
                    typeSelectorOptions = listOf(
                        "Task",
                        "Resource",
                        "Folder",
                        "Bookmark",
                        "Area"
                    ),
                    statusSelectorOptions = listOf(
                        "Focus",
                        "Next week",
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
