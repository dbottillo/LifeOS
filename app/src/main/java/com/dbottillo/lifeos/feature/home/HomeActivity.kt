package com.dbottillo.lifeos.feature.home

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.window.core.layout.WindowWidthSizeClass
import com.dbottillo.lifeos.BuildConfig
import com.dbottillo.lifeos.R
import com.dbottillo.lifeos.data.AppConstant
import com.dbottillo.lifeos.feature.articles.ArticlesScreen
import com.dbottillo.lifeos.feature.composer.ComposerInput
import com.dbottillo.lifeos.feature.composer.TaskComposerScreen
import com.dbottillo.lifeos.feature.composer.TaskComposerScreenDialog
import com.dbottillo.lifeos.feature.composer.TaskComposerViewModel
import com.dbottillo.lifeos.feature.status.StatusScreen
import com.dbottillo.lifeos.feature.status.StatusViewModel
import com.dbottillo.lifeos.ui.AppTheme
import com.dbottillo.lifeos.util.openLink
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Locale

@Suppress("LongMethod")
@ExperimentalMaterial3Api
@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private val statusViewModel: StatusViewModel by viewModels()
    private val taskComposerViewModel: TaskComposerViewModel by viewModels()

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val items = listOf(
                Screen.Home,
                Screen.Articles,
                Screen.Status,
            )
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
            var bottomBarVisible by remember { mutableStateOf(true) }
            AppTheme {
                Scaffold(
                    topBar = { TopBarForDestination(navController, currentDestination) },
                    bottomBar = {
                        AnimatedVisibility(
                            visible = bottomBarVisible,
                            enter = slideInVertically(initialOffsetY = { it }),
                            exit = slideOutVertically(targetOffsetY = { it })
                        ) {
                            NavigationBar {
                                items.forEach { screen ->
                                    NavigationBarItem(
                                        icon = {
                                            Icon(
                                                painterResource(id = screen.iconId),
                                                contentDescription = null
                                            )
                                        },
                                        label = { Text(stringResource(screen.resourceId)) },
                                        selected = currentDestination?.hierarchy?.any {
                                            it.route == screen.route
                                        } == true,
                                        onClick = {
                                            navController.navigate(screen.route) {
                                                // Pop up to the start destination of the graph to
                                                // avoid building up a large stack of destinations
                                                // on the back stack as users select items
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                // Avoid multiple copies of the same destination when
                                                // reselecting the same item
                                                launchSingleTop = true
                                                // Restore state when reselecting a previously selected item
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    },
                    floatingActionButton = {
                        if (currentDestination?.route == Screen.Home.route) {
                            FloatingActionButton(
                                containerColor = Color.Yellow,
                                contentColor = Color.Black,
                                onClick = {
                                    if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED) {
                                        navController.navigate(ComposerDialog(null))
                                    } else {
                                        navController.navigate(Composer(null)) {
                                            launchSingleTop = true
                                            restoreState = false
                                        }
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

                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Home.route) {
                            bottomBarVisible = true
                            HomeScreen(
                                navController,
                                homeViewModel
                            )
                        }
                        composable(Screen.Articles.route) {
                            bottomBarVisible = true
                            ArticlesScreen(
                                navController,
                                homeViewModel
                            )
                        }
                        composable(Screen.Status.route) {
                            bottomBarVisible = true
                            StatusScreen(
                                navController,
                                statusViewModel,
                                dateFormatter
                            )
                        }
                        composable<Composer> { backStackEntry ->
                            bottomBarVisible = false
                            val composer = backStackEntry.toRoute<Composer>()
                            val entryId = composer.entryId
                            taskComposerViewModel.init(ComposerInput(entryId = entryId))
                            TaskComposerScreen(
                                navController = navController,
                                viewModel = taskComposerViewModel
                            )
                        }
                        dialog<ComposerDialog> { backStackEntry ->
                            bottomBarVisible = true
                            val composer = backStackEntry.toRoute<Composer>()
                            val entryId = composer.entryId
                            taskComposerViewModel.init(ComposerInput(entryId = entryId))
                            TaskComposerScreenDialog(
                                navController = navController,
                                viewModel = taskComposerViewModel
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TopBarForDestination(
        navController: NavController,
        currentDestination: NavDestination?
    ) {
        val context = LocalContext.current
        when (currentDestination?.route) {
            "composer" -> {
                TopAppBar(
                    title = { Text("Composer") },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
                        }
                    }
                )
            }

            Screen.Articles.route -> {
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

@Serializable
sealed class Screen(
    val route: String,
    @StringRes val resourceId: Int,
    @DrawableRes val iconId: Int
) {
    data object Home : Screen("home", R.string.home, R.drawable.baseline_sun_24)
    data object Articles : Screen("articles", R.string.articles, R.drawable.baseline_list_24)
    data object Status : Screen("status", R.string.status, R.drawable.baseline_settings_24)
}

@Serializable data class Composer(val entryId: String?)

@Serializable data class ComposerDialog(val entryId: String?)
