package com.topespinf.collabtask.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.topespinf.collabtask.R
import com.topespinf.collabtask.navigation.AppNavGraph
import com.topespinf.collabtask.navigation.AppRoute
import com.topespinf.collabtask.repositories.AuthRepository
import com.topespinf.collabtask.repositories.SessionRepository
import com.topespinf.collabtask.ui.components.CollabBottomBar
import com.topespinf.collabtask.ui.theme.CollabTaskTheme
import com.topespinf.collabtask.viewmodel.NotificationViewModel
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CollabTaskApp() {
    val navController = rememberNavController()
    val notificationViewModel: NotificationViewModel = viewModel()
    val notifications by notificationViewModel.notifications.collectAsState()
    val drawerState = rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val unreadCount = notifications.count { !it.read }
    LaunchedEffect(Unit) {
        SessionRepository.bootstrapFromFirebase()
        runCatching {
            AuthRepository.refreshCurrentUserProfile()
        }
    }
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route
    val mainRoutes = setOf(AppRoute.Tasks.route, AppRoute.Profile.route)
    val showTopBar = currentRoute in mainRoutes
    val showBottomBar = currentRoute in mainRoutes

    CollabTaskTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = showTopBar,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(252.dp)
                ) {
                    Text(
                        text = "Notificações",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                    if (notifications.isEmpty()) {
                        Text(
                            text = "Nenhuma notificação no momento.",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(notifications.take(30), key = { it.id }) { notification ->
                                Text(
                                    text = "• ${notification.message}",
                                    style = if (notification.read) {
                                        MaterialTheme.typography.bodySmall
                                    } else {
                                        MaterialTheme.typography.bodyMedium
                                    },
                                    modifier = Modifier
                                        .background(
                                            if (notification.read) {
                                                MaterialTheme.colorScheme.surface
                                            } else {
                                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                                            },
                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                                        )
                                        .padding(10.dp)
                                )
                            }
                        }
                    }
                }
            },
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    if (showTopBar) {
                        CenterAlignedTopAppBar(
                            title = {
                                Image(
                                    painter = painterResource(id = R.drawable.logo_collabtask),
                                    contentDescription = "CollabTask",
                                    modifier = Modifier.height(72.dp),
                                    contentScale = ContentScale.Fit
                                )
                            },
                            actions = {
                                IconButton(
                                    onClick = {
                                        notificationViewModel.markAllAsRead()
                                        scope.launch { drawerState.open() }
                                    }
                                ) {
                                    BadgedBox(
                                        badge = {
                                            if (unreadCount > 0) {
                                                Badge()
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Notifications,
                                            contentDescription = "Abrir notificações"
                                        )
                                    }
                                }
                            }
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
}


