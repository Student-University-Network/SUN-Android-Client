package com.sun.sunclient

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sun.sunclient.application.MainViewModel
import com.sun.sunclient.ui.screens.course.CoursesScreen
import com.sun.sunclient.ui.screens.home.HomeScreen
import com.sun.sunclient.ui.screens.profile.ProfileScreen
import com.sun.sunclient.ui.shared.TopBar
import com.sun.sunclient.utils.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    startDestination: Screen = Screen.HOME,
    mainViewModel: MainViewModel = viewModel()
) {
    val navController: NavHostController = rememberNavController()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.HOME) }

    fun navigateToProfile() {
        navController.navigate(Screen.PROFILE.route) {
            popUpTo(Screen.HOME.route)
            currentScreen = Screen.PROFILE
        }
    }

    fun navigateToHome() {
        navController.navigate(Screen.HOME.route) {
            popUpTo(Screen.HOME.route) { inclusive = true }
            currentScreen = Screen.HOME
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(
                currentScreen = currentScreen,
                onProfileClick = { navigateToProfile() },
                onBackClick = { navigateToHome() }
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(navController = navController, startDestination = startDestination.route) {
                composable(Screen.HOME.route) {
                    HomeScreen(
                        onNavigateToProfile = { navigateToProfile() },
                        mainViewModel = mainViewModel
                    )
                }
                composable(Screen.PROFILE.route) {
                    ProfileScreen(
                        onNavigateToHome = { navigateToHome() },
                        mainViewModel = mainViewModel
                    )
                }
                composable(Screen.COURSES.route) {
                    CoursesScreen(
                        onNavigateToHome = { navigateToHome() },
                        mainViewModel = mainViewModel
                    )
                }
                // TODO: add rest of routes
            }
        }
    }
}

