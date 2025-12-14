package com.dbottillo.lifeos.feature.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.dbottillo.lifeos.data.AppConstant
import com.dbottillo.lifeos.feature.articles.ArticlesScreenNav3
import com.dbottillo.lifeos.feature.review.ReviewScreenNav3
import com.dbottillo.lifeos.feature.review.ReviewViewModel
import com.dbottillo.lifeos.feature.status.StatusScreenNav3
import com.dbottillo.lifeos.feature.status.StatusViewModel
import com.dbottillo.lifeos.ui.AppTheme
import com.dbottillo.lifeos.util.openLink
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.navigation3.runtime.NavKey
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import com.dbottillo.lifeos.feature.composer.ComposerInput
import com.dbottillo.lifeos.feature.composer.TaskComposerActivity
import com.dbottillo.lifeos.feature.composer.TaskComposerScreenDialogNav3
import com.dbottillo.lifeos.feature.composer.TaskComposerViewModel
import kotlinx.serialization.Serializable
import kotlin.to

data class TopLevelRoute(
    val icon: Int,
    val label: String
)

@Serializable
data object Home : NavKey

@Serializable
data object Review : NavKey

@Serializable
data object Articles : NavKey

@Serializable
data object Status : NavKey

@Serializable
data class ComposerDialog(val entryId: String?) : NavKey

private val TOP_LEVEL_ROUTES = mapOf<NavKey, TopLevelRoute>(
    Home to TopLevelRoute(icon = R.drawable.baseline_sun_24, label = "Home"),
    Review to TopLevelRoute(icon = R.drawable.review_24, label = "Review"),
    Articles to TopLevelRoute(icon = R.drawable.baseline_list_24, label = "Articles"),
    Status to TopLevelRoute(icon = R.drawable.baseline_settings_24, label = "Status"),
)


@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private val homeViewModel: HomeViewModel by viewModels()

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS", Locale.getDefault())

    @Suppress("LongMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val topLevelBackStack = remember { TopLevelBackStack<NavKey>(Home) }
            val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
            AppTheme {
                Scaffold(
                    topBar = { TopBarForDestination(topLevelBackStack) },
                    floatingActionButton = {
                        if (topLevelBackStack.topLevelKey == Home) {
                            FloatingActionButton(
                                containerColor = Color.Yellow,
                                contentColor = Color.Black,
                                onClick = {
                                    if (windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)) {
                                        topLevelBackStack.add(ComposerDialog(null))
                                    } else {
                                        this@HomeActivity.startActivity(
                                            Intent(this@HomeActivity, TaskComposerActivity::class.java)
                                        )
                                    }
                                },
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_add_24),
                                    "Floating action button."
                                )
                            }
                        }
                    },
                    bottomBar = {
                        NavigationBar {
                            TOP_LEVEL_ROUTES.forEach { (key,route) ->
                                val isSelected = key == topLevelBackStack.topLevelKey
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        topLevelBackStack.addTopLevel(key)
                                    },
                                    label = { Text(route.label) },
                                    icon = {
                                        Icon(
                                            painterResource(id = route.icon),
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
                                HomeScreenNav3(homeViewModel)
                            }
                            entry<Review> {
                                val viewModel: ReviewViewModel by viewModels()
                                ReviewScreenNav3(viewModel)
                            }
                            entry<Articles> {
                                ArticlesScreenNav3(homeViewModel)
                            }
                            entry<Status> {
                                val viewModel: StatusViewModel by viewModels()
                                StatusScreenNav3(viewModel, dateFormatter)
                            }
                            entry<ComposerDialog> { key ->
                                val viewModel: TaskComposerViewModel by viewModels()
                                viewModel.init(ComposerInput(entryId = key.entryId))
                                TaskComposerScreenDialogNav3(
                                    viewModel = viewModel,
                                    close = {
                                        topLevelBackStack.removeLast()
                                    }
                                )
                            }
                        },
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun TopBarForDestination(
        topLevelBackStack: TopLevelBackStack<NavKey>,
    ) {
        val context = LocalContext.current
        when (topLevelBackStack.topLevelKey) {
            is Articles -> {
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
            }
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
}

class TopLevelBackStack<T : NavKey>(startKey: T) {

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
