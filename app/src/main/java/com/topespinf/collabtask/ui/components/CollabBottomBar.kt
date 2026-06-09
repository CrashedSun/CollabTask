package com.topespinf.collabtask.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.topespinf.collabtask.R
import com.topespinf.collabtask.navigation.AppRoute

data class BottomItem(
    val route: AppRoute,
    val labelResId: Int,
    val icon: @Composable () -> Unit
)

private val bottomItems = listOf(
    BottomItem(AppRoute.Tasks, R.string.tab_tasks) { Icon(Icons.AutoMirrored.Outlined.ListAlt, contentDescription = null) },
    BottomItem(AppRoute.Profile, R.string.tab_profile) { Icon(Icons.Outlined.Person, contentDescription = null) }
)

@Composable
fun CollabBottomBar(
    currentRoute: String?,
    onNavigate: (AppRoute) -> Unit
) {
    NavigationBar {
        bottomItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route.route,
                onClick = { onNavigate(item.route) },
                icon = item.icon,
                label = { Text(text = stringResource(item.labelResId)) }
            )
        }
    }
}


