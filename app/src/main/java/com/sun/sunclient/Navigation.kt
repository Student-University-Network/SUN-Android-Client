package com.sun.sunclient

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sun.sunclient.application.MainViewModel
import com.sun.sunclient.ui.screens.home.HomeScreen
import com.sun.sunclient.ui.screens.profile.ProfileScreen
import com.sun.sunclient.utils.Screens

@Composable
fun AppNavigation(
    startDestination: String = Screens.HOME,
    mainViewModel: MainViewModel = viewModel()
) {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screens.HOME) {
            HomeScreen(onNavigateToProfile = {
                navController.navigate(Screens.PROFILE) {
                    popUpTo(Screens.HOME)
                }
            })
        }
        composable(Screens.PROFILE) {
            ProfileScreen(onNavigateToHome = {
                navController.navigate(Screens.HOME) {
                    popUpTo(Screens.HOME) { inclusive = true }
                }
            }, mainViewModel = mainViewModel)
        }
    }
}