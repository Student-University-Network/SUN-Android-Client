package com.sun.sunclient

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sun.sunclient.ui.screens.course.CoursePage
import com.sun.sunclient.ui.screens.course.CoursesScreen
import com.sun.sunclient.ui.screens.home.HomeScreen
import com.sun.sunclient.ui.screens.login.LoginScreen
import com.sun.sunclient.ui.screens.profile.ProfileScreen
import com.sun.sunclient.ui.shared.TopBar
import com.sun.sunclient.utils.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    startDestination: Screen = Screen.LOG_IN,
    mainViewModel: MainViewModel,
) {
    // navigation and scaffold
    val navController: NavHostController = rememberNavController()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.LOG_IN) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    var topBarVisibility by remember { mutableStateOf(false) }

    fun navigateAndClearStack(screen: Screen) {
        navController.navigate(screen.route) {
            popUpTo(navController.graph.id) { inclusive = true }
            currentScreen = screen
        }
    }

    // navigate when login changed
    LaunchedEffect(key1 = mainViewModel.loggedIn) {
        if (mainViewModel.loggedIn) {
            navigateAndClearStack(Screen.HOME)
        } else {
            navigateAndClearStack(Screen.LOG_IN)
        }
    }

    // update currentScreen with navigation to display title in Topbar
    LaunchedEffect(key1 = navController.currentDestination) {
        currentScreen = when (navController.currentDestination?.route) {
            Screen.PROFILE.route -> Screen.PROFILE
            Screen.COURSEPAGE.route -> Screen.COURSEPAGE
            Screen.COURSES.route -> Screen.COURSES
            Screen.ANNOUCEMENTS.route -> Screen.ANNOUCEMENTS
            Screen.ATTENDANCE.route -> Screen.ATTENDANCE
            Screen.DISUCSSION.route -> Screen.DISUCSSION
            Screen.TIMETABLE.route -> Screen.TIMETABLE
            else -> Screen.HOME
        }
    }

    // only hide topbar for LoginScreen
    topBarVisibility = when (navBackStackEntry?.destination?.route) {
        null -> false
        Screen.LOG_IN.route -> false
        else -> true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AnimatedVisibility(
                visible = topBarVisibility,
                enter = slideInVertically(
                    animationSpec = tween(500),
                    initialOffsetY = { fullHeight -> -fullHeight }
                ),
                exit = slideOutVertically(
                    animationSpec = tween(500),
                    targetOffsetY = { fullHeight -> fullHeight }
                )
            ) {
                if (topBarVisibility) {
                    TopBar(
                        currentScreen = currentScreen,
                        onProfileClick = {
                            navController.navigate(Screen.PROFILE.route) {
                                popUpTo(Screen.HOME.route)
                            }
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(navController = navController, startDestination = startDestination.route) {
                composable(Screen.LOG_IN.route) {
                    LoginScreen(
                        setLoggedIn = { mainViewModel.setLoggedIn() },
                    )
                }
                composable(Screen.HOME.route) {
                    HomeScreen(
                        onNavigateToProfile = {
                            navController.navigate(Screen.PROFILE.route) {
                                popUpTo(Screen.HOME.route)
                            }
                        },
                        onNavigateToCourses = {
                            navController.navigate(Screen.COURSES.route) {
                                popUpTo(Screen.HOME.route)
                            }
                        },
                        mainViewModel = mainViewModel
                    )
                }
                composable(Screen.PROFILE.route) {
                    ProfileScreen(mainViewModel = mainViewModel)
                }
                composable(Screen.COURSES.route) {
                    CoursesScreen(
                        navigateInScreen = { screen ->
                            navController.navigate(screen.route) { launchSingleTop = true }
                        }
                    )
                }
                composable(Screen.COURSEPAGE.route) {
                    CoursePage()
                }
                // TODO: add rest of routes
            }
        }
    }
}

