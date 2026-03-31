package com.topespinf.collabtask.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.topespinf.collabtask.ui.screens.auth.LoginScreen
import com.topespinf.collabtask.ui.screens.auth.RegisterScreen
import com.topespinf.collabtask.ui.screens.auth.ResetPasswordScreen
import com.topespinf.collabtask.ui.screens.home.HomeScreen
import com.topespinf.collabtask.ui.screens.profile.ProfileScreen
import com.topespinf.collabtask.ui.screens.tasks.TasksScreen
import com.topespinf.collabtask.ui.screens.usage.UsageGuideScreen

fun NavHostController.navigateTo(route: AppRoute) {
    navigate(route.route)
}

@androidx.compose.runtime.Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Login.route,
        modifier = modifier
    ) {
        composable(AppRoute.Login.route) {
            LoginScreen(
                onLoginClick = {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(AppRoute.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate(AppRoute.Register.route) },
                onForgotPasswordClick = { navController.navigate(AppRoute.ResetPassword.route) }
            )
        }
        composable(AppRoute.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(AppRoute.Login.route) { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }
        composable(AppRoute.Home.route) {
            HomeScreen(
                onGetStartedClick = { navController.navigate(AppRoute.UsageGuide.route) }
            )
        }
        composable(AppRoute.Tasks.route) {
            TasksScreen()
        }
        composable(AppRoute.Profile.route) {
            ProfileScreen(
                onChangePasswordClick = { navController.navigate(AppRoute.ResetPassword.route) }
            )
        }
        composable(AppRoute.UsageGuide.route) {
            UsageGuideScreen(
                onContinue = { navController.popBackStack() }
            )
        }
        composable(AppRoute.ResetPassword.route) {
            ResetPasswordScreen(
                onConfirm = { navController.popBackStack() }
            )
        }
    }
}

