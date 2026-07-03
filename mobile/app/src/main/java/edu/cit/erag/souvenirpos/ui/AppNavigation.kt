package edu.cit.erag.souvenirpos.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.cit.erag.souvenirpos.di.ServiceLocator
import edu.cit.erag.souvenirpos.ui.login.LoginScreen
import edu.cit.erag.souvenirpos.ui.pos.PosScreen

object Routes {
    const val LOGIN = "login"
    const val POS = "pos"
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
            )
        }
    }
}
