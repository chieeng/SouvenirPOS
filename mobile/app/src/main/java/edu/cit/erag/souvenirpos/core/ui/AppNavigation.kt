package edu.cit.erag.souvenirpos.core.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.cit.erag.souvenirpos.core.di.ServiceLocator
import edu.cit.erag.souvenirpos.categories.ui.ManageCategoriesScreen
import edu.cit.erag.souvenirpos.dashboard.ui.DashboardScreen
import edu.cit.erag.souvenirpos.history.ui.SalesHistoryScreen
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
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val start = if (ServiceLocator.tokenStore.isLoggedIn()) Routes.POS else Routes.LOGIN

    NavHost(navController = navController, startDestination = start) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = {
                    navController.navigate(Routes.POS) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
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
