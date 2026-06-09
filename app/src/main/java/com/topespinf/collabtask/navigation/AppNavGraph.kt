package com.topespinf.collabtask.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.topespinf.collabtask.repositories.SessionRepository
import com.topespinf.collabtask.ui.screens.auth.LoginScreen
import com.topespinf.collabtask.ui.screens.auth.RegisterScreen
import com.topespinf.collabtask.ui.screens.auth.ResetPasswordScreen
import com.topespinf.collabtask.ui.screens.home.HomeScreen
import com.topespinf.collabtask.ui.screens.profile.ProfileScreen
import com.topespinf.collabtask.ui.screens.tasks.CreateTaskDialog
import com.topespinf.collabtask.ui.screens.tasks.EditTaskDialogCompose
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
        startDestination = if (SessionRepository.isLoggedIn()) AppRoute.Tasks.route else AppRoute.Home.route,
        modifier = modifier
    ) {
        composable(AppRoute.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppRoute.Tasks.route) {
                        popUpTo(AppRoute.Home.route) { inclusive = false }
                    }
                },
                onRegisterClick = { navController.navigate(AppRoute.Register.route) },
                onForgotPasswordClick = { navController.navigate(AppRoute.ResetPassword.route) }
            )
        }
        composable(AppRoute.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(AppRoute.Tasks.route) {
                        popUpTo(AppRoute.Home.route) { inclusive = false }
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }
        composable(AppRoute.Home.route) {
            HomeScreen(
                onGetStartedClick = { navController.navigate(AppRoute.Login.route) }
            )
        }
        composable(AppRoute.Tasks.route) {
            TasksScreen(
                onCreateTaskClick = { navController.navigate(AppRoute.CreateTask.route) },
                onEditTaskClick = { taskId, fromAdminView ->
                    navController.navigate(AppRoute.editTaskRoute(taskId, fromAdminView))
                }
            )
        }
        composable(AppRoute.Profile.route) {
            ProfileScreen(
                onLogoutClick = {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            )
        }
        composable(AppRoute.UsageGuide.route) {
            UsageGuideScreen(
                onContinue = { navController.popBackStack() }
            )
        }
        composable(AppRoute.CreateTask.route) {
            CreateTaskDialog(
                onDismiss = { navController.popBackStack() },
                onSave = { navController.popBackStack() }
            )
        }
        composable(
            route = "${AppRoute.EditTask.route}/{taskId}/{fromAdminView}",
            arguments = listOf(
                navArgument("taskId") { type = NavType.StringType },
                navArgument("fromAdminView") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId").orEmpty()
            val fromAdminView = backStackEntry.arguments?.getBoolean("fromAdminView") ?: false
            EditTaskDialogCompose(
                taskId = taskId,
                canManageAdmins = fromAdminView,
                onDismiss = { navController.popBackStack() },
                onSave = { navController.popBackStack() }
            )
        }
        composable(AppRoute.ResetPassword.route) {
            ResetPasswordScreen(
                onConfirm = { navController.popBackStack() }
            )
        }
    }
}

