package com.dbottillo.lifeos.feature.home

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dbottillo.lifeos.R
import com.dbottillo.lifeos.data.AppConstant
import com.dbottillo.lifeos.feature.articles.ArticlesScreen
import com.dbottillo.lifeos.feature.status.StatusScreen
import com.dbottillo.lifeos.ui.AppTheme
import com.dbottillo.lifeos.util.openLink
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale

@Suppress("LongMethod")
@ExperimentalMaterial3Api
@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private val homeViewModel: HomeViewModel by viewModels()

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val items = listOf(
                Screen.Articles,
                Screen.Status,
            )
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val context = LocalContext.current
            AppTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Life OS") },
                            actions = {
                                if (currentDestination?.hierarchy?.any { it.route == Screen.Articles.route } == true) {
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
                            }
                        )
                    },
                    bottomBar = {
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
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
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
                ) { innerPadding ->
                    NavHost(
                        navController,
                        startDestination = Screen.Articles.route,
                        Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Articles.route) {
                            ArticlesScreen(
                                navController,
                                homeViewModel
                            )
                        }
                        composable(Screen.Status.route) {
                            StatusScreen(
                                navController,
                                homeViewModel,
                                dateFormatter
                            )
                        }
                    }
                }
            }
        }
    }
}

sealed class Screen(val route: String, @StringRes val resourceId: Int, @DrawableRes val iconId: Int) {
    data object Articles : Screen("articles", R.string.articles, R.drawable.baseline_list_24)
    data object Status : Screen("status", R.string.status, R.drawable.baseline_settings_24)
}
