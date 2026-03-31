package com.topespinf.collabtask.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.topespinf.collabtask.R
import com.topespinf.collabtask.navigation.AppNavGraph
import com.topespinf.collabtask.navigation.AppRoute
import com.topespinf.collabtask.ui.components.CollabBottomBar
import com.topespinf.collabtask.ui.theme.CollabTaskTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CollabTaskApp() {
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route
    val mainRoutes = setOf(AppRoute.Home.route, AppRoute.Tasks.route, AppRoute.Profile.route)
    val showTopBar = currentRoute in mainRoutes
    val showBottomBar = currentRoute in mainRoutes

    CollabTaskTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (showTopBar) {
                    TopAppBar(
                        title = { Text(text = stringResource(R.string.app_name)) }
                    )
                }
            },
            bottomBar = {
                if (showBottomBar) {
                    CollabBottomBar(currentRoute = currentRoute) { route ->
                        navController.navigate(route.route) {
                            launchSingleTop = true
                        }
                    }
                }
            }
        ) { paddingValues ->
            AppNavGraph(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                navController = navController
            )
        }
    }
}


