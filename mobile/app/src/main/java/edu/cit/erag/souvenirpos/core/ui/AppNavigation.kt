package edu.cit.erag.souvenirpos.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.cit.erag.souvenirpos.core.di.ServiceLocator
import edu.cit.erag.souvenirpos.categories.ui.ManageCategoriesScreen
import edu.cit.erag.souvenirpos.dashboard.ui.DashboardScreen
import edu.cit.erag.souvenirpos.history.ui.SalesHistoryScreen
import edu.cit.erag.souvenirpos.auth.ui.ChangePasswordScreen
import edu.cit.erag.souvenirpos.auth.ui.LoginScreen
import edu.cit.erag.souvenirpos.pos.ui.PosScreen
import edu.cit.erag.souvenirpos.users.ui.ManageUsersScreen

object Routes {
    const val LOGIN = "login"
    const val POS = "pos"
    const val USERS = "users"
    const val HISTORY = "history"
    const val DASHBOARD = "dashboard"
    const val CATEGORIES = "categories"
    const val CHANGE_PASSWORD = "change_password"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val tokenStore = ServiceLocator.tokenStore
    val start = when {
        tokenStore.isLoggedIn() && tokenStore.mustChangePassword() -> Routes.CHANGE_PASSWORD
        tokenStore.isLoggedIn() -> Routes.POS
        else -> Routes.LOGIN
    }

    // A rejected token (expired/revoked/deactivated) clears the session; bounce to login.
    LaunchedEffect(Unit) {
        ServiceLocator.unauthorizedEvents.collect {
            navController.navigate(Routes.LOGIN) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(navController = navController, startDestination = start) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = { mustChangePassword ->
                    val destination = if (mustChangePassword) Routes.CHANGE_PASSWORD else Routes.POS
                    navController.navigate(destination) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.CHANGE_PASSWORD) {
            // Forced when the stored session still carries the must-change flag; otherwise the
            // user reached here voluntarily from the POS menu.
            val forced = tokenStore.mustChangePassword()
            ChangePasswordScreen(
                forced = forced,
                onChanged = {
                    navController.navigate(Routes.POS) {
                        popUpTo(Routes.CHANGE_PASSWORD) { inclusive = true }
                    }
                },
                onCancel = {
                    if (forced) {
                        // No usable session yet: cancelling signs out.
                        ServiceLocator.authRepository.logout()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    } else {
                        // Voluntary visit: just return to where they came from.
                        navController.popBackStack()
                    }
                },
            )
        }
        composable(Routes.POS) {
            PosScreen(
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.POS) { inclusive = true }
                    }
                },
                onManageUsers = { navController.navigate(Routes.USERS) },
                onViewHistory = { navController.navigate(Routes.HISTORY) },
                onViewDashboard = { navController.navigate(Routes.DASHBOARD) },
                onManageCategories = { navController.navigate(Routes.CATEGORIES) },
                onChangePassword = { navController.navigate(Routes.CHANGE_PASSWORD) },
            )
        }
        composable(Routes.USERS) {
            ManageUsersScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.HISTORY) {
            SalesHistoryScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.CATEGORIES) {
            ManageCategoriesScreen(onBack = { navController.popBackStack() })
        }
    }
}
