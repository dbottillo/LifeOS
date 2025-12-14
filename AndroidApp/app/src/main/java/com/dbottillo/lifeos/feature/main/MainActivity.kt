package com.dbottillo.lifeos.feature.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.dbottillo.lifeos.BuildConfig
import com.dbottillo.lifeos.R
import com.dbottillo.lifeos.feature.home.HomeScreenNav3
import com.dbottillo.lifeos.feature.home.HomeViewModel
import com.dbottillo.lifeos.feature.status.StatusScreenNav3
import com.dbottillo.lifeos.feature.status.StatusViewModel
import com.dbottillo.lifeos.ui.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale

private sealed interface TopLevelRoute {
    val icon: Int
    val label: String
}
private data object Home : TopLevelRoute {
    override val icon = R.drawable.baseline_sun_24
    override val label = "Home"
}
private data object Status : TopLevelRoute {
    override val icon = R.drawable.baseline_settings_24
    override val label = "Status"
}

private val TOP_LEVEL_ROUTES: List<TopLevelRoute> = listOf(Home, Status)

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // LifeOSMainApp()

            val topLevelBackStack = remember { TopLevelBackStack<Any>(Home) }
            AppTheme {
                Scaffold(
                    topBar = { TopBarForDestination(topLevelBackStack) },
                    bottomBar = {
                        NavigationBar {
                            TOP_LEVEL_ROUTES.forEach { topLevelRoute ->
                                val isSelected = topLevelRoute == topLevelBackStack.topLevelKey
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        topLevelBackStack.addTopLevel(topLevelRoute)
                                    },
                                    label = { Text(topLevelRoute.label) },
                                    icon = {
                                        Icon(
                                            painterResource(id = topLevelRoute.icon),
                                            contentDescription = null
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedTextColor = MaterialTheme.colorScheme.secondary,
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavDisplay(
                        backStack = topLevelBackStack.backStack,
                        onBack = { topLevelBackStack.removeLast() },
                        entryDecorators = listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator()
                        ),
                        modifier = Modifier.padding(innerPadding),
                        entryProvider = entryProvider {
                            entry<Home> {
                                val viewModel: HomeViewModel by viewModels()
                                HomeScreenNav3(viewModel)
                            }
                            entry<Status> {
                                val viewModel: StatusViewModel by viewModels()
                                StatusScreenNav3(viewModel, dateFormatter)
                            }
                        },
                    )
                }
            }
        }
    }
}

class TopLevelBackStack<T : Any>(startKey: T) {

    // Maintain a stack for each top level route
    private var topLevelStacks: LinkedHashMap<T, SnapshotStateList<T>> = linkedMapOf(
        startKey to mutableStateListOf(startKey)
    )

    // Expose the current top level route for consumers
    var topLevelKey by mutableStateOf(startKey)
        private set

    // Expose the back stack so it can be rendered by the NavDisplay
    val backStack = mutableStateListOf(startKey)

    private fun updateBackStack() =
        backStack.apply {
            clear()
            addAll(topLevelStacks.flatMap { it.value })
        }

    fun addTopLevel(key: T) {
        // If the top level doesn't exist, add it
        if (topLevelStacks[key] == null) {
            topLevelStacks[key] = mutableStateListOf(key)
        } else {
            // Otherwise just move it to the end of the stacks
            topLevelStacks.apply {
                remove(key)?.let {
                    put(key, it)
                }
            }
        }
        topLevelKey = key
        updateBackStack()
    }

    fun add(key: T) {
        topLevelStacks[topLevelKey]?.add(key)
        updateBackStack()
    }

    fun removeLast() {
        val removedKey = topLevelStacks[topLevelKey]?.removeLastOrNull()
        // If the removed key was a top level key, remove the associated top level stack
        topLevelStacks.remove(removedKey)
        topLevelKey = topLevelStacks.keys.last()
        updateBackStack()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBarForDestination(
    topLevelBackStack: TopLevelBackStack<Any>,
) {
    when (topLevelBackStack.topLevelKey) {
        /*"composer" -> {
            TopAppBar(
                title = { Text("Composer") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
                    }
                }
            )
        }*/

        /*is Status -> {
            TopAppBar(
                title = {
                    Text(
                        buildAnnotatedString {
                            append("Life OS ")
                            withStyle(
                                style = SpanStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Light
                                )
                            ) {
                                append(BuildConfig.VERSION_NAME)
                            }
                        }
                    )
                },
                actions = {
                    IconButton(onClick = {
                        context.openLink(AppConstant.NOTION_ARTICLE_PAGE_URL)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_external),
                            contentDescription = null
                        )
                    }
                    IconButton(onClick = { homeViewModel.load() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_reload),
                            contentDescription = null
                        )
                    }
                }
            )
        }*/
        else -> {
            TopAppBar(
                title = {
                    Text(
                        buildAnnotatedString {
                            append("Life OS ")
                            withStyle(
                                style = SpanStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Light
                                )
                            ) {
                                append(BuildConfig.VERSION_NAME)
                            }
                        }
                    )
                },
            )
        }
    }
}
