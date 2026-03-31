package com.topespinf.collabtask.navigation

sealed class AppRoute(val route: String) {
    data object Login : AppRoute("login")
    data object Register : AppRoute("register")
    data object Home : AppRoute("home")
    data object Tasks : AppRoute("tasks")
    data object Profile : AppRoute("profile")
    data object UsageGuide : AppRoute("usage_guide")
    data object ResetPassword : AppRoute("reset_password")
}

