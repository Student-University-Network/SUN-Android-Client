package com.sun.sunclient

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat.getSystemService
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sun.sunclient.config.Config
import com.sun.sunclient.network.schemas.Program
import com.sun.sunclient.ui.screens.course.CoursePage
import com.sun.sunclient.ui.screens.course.CoursesScreen
import com.sun.sunclient.ui.screens.home.HomeScreen
import com.sun.sunclient.ui.screens.login.LoginScreen
import com.sun.sunclient.ui.screens.profile.ProfileScreen
import com.sun.sunclient.ui.shared.ErrorOverlay
import com.sun.sunclient.ui.screens.splash.SplashScreen
import com.sun.sunclient.ui.shared.TopBar
import com.sun.sunclient.ui.theme.SUNTheme
import com.sun.sunclient.utils.Screen
import com.sun.sunclient.utils.AppEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

val LocalSnackbar = compositionLocalOf<SnackbarHostState> { error("No SnackbarHostState Provided") }

object MyEvents {
    val eventFlow = Channel<AppEvent>()
    val eventFlowListener = eventFlow.receiveAsFlow()
}

data class OverlayDialogState(
    val visible: Boolean = false,
    val msg: String = "Something went wrong",
    val icon: Int = R.drawable.im_empty_page,
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val mainViewModel: MainViewModel = hiltViewModel()
            val coroutineScope = rememberCoroutineScope()

            // navigation and scaffold
            val navController: NavHostController = rememberNavController()
            var currentScreen by remember { mutableStateOf<Screen>(Screen.LOG_IN) }
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            var topBarVisibility by remember { mutableStateOf(false) }
            val snackbarHostState = remember { SnackbarHostState() }
            var errorOverlayState by remember { mutableStateOf(OverlayDialogState()) }

            fun navigateAndClearStack(screen: Screen) {
                navController.navigate(screen.route) {
                    popUpTo(navController.graph.id) { inclusive = true }
                    currentScreen = screen
                }
            }

            fun connectivityCheck() {
                val scope = CoroutineScope(Dispatchers.IO)
                scope.launch {
                    errorOverlayState = OverlayDialogState()
                    val connectionAvailable = connectionAvailability()
                    val apiAvailable = checkAPIAvailability()

                    if (!connectionAvailable) {
                        errorOverlayState = OverlayDialogState(
                            visible = true,
                            msg = "No connectivity available",
                            icon = R.drawable.ic_no_connection
                        )
                        val actionClicked = snackbarHostState.showSnackbar(
                            "No connectivity available",
                            "Retry",
                            duration = SnackbarDuration.Indefinite
                        )
                        if (actionClicked == SnackbarResult.ActionPerformed) {
                            connectivityCheck()
                        }
                        return@launch
                    }

                    if (apiAvailable) {
                        mainViewModel.syncData()
                    } else {
                        errorOverlayState = OverlayDialogState(
                            visible = true,
                            msg = "Server not available. Try again later",
                            icon = R.drawable.ic_server_down
                        )
                        val actionClicked = snackbarHostState.showSnackbar(
                            "Server not available",
                            "Retry",
                            duration = SnackbarDuration.Indefinite
                        )
                        if (actionClicked == SnackbarResult.ActionPerformed) {
                            connectivityCheck()
                        }
                        return@launch
                    }
                }
            }

            LaunchedEffect(key1 = true) {
                connectivityCheck()
                mainViewModel.onStart()
            }

            LaunchedEffect(key1 = true) {
                MyEvents.eventFlowListener.collectLatest { event ->
                    when (event) {
                        is AppEvent.OnLogin -> {
                            navigateAndClearStack(Screen.HOME)
                        }
                        is AppEvent.OnLogout -> {
                            navigateAndClearStack(Screen.LOG_IN)
                        }
                        is AppEvent.PopBackStack -> {
                            navController.popBackStack()
                        }
                        is AppEvent.Navigate -> {
                            navController.navigate(event.route.route) {
                                popUpTo(Screen.HOME.route)
                            }
                            currentScreen = event.route
                        }
                        is AppEvent.SnackBar -> {
                            coroutineScope.launch {
                                val status = snackbarHostState.showSnackbar(
                                    event.text,
                                    event.actionLabel,
                                    event.showDismissAction,
                                    event.duration
                                )
                                if (status == SnackbarResult.ActionPerformed) {
                                    event.resolve()
                                }
                            }
                        }
//                        is AppEvent.OnSyncData -> {
//                            mainViewModel.syncData()
//                        }
                    }
                }
            }

            // only hide topbar for LoginScreen
            topBarVisibility = when (navBackStackEntry?.destination?.route) {
                null -> false
                Screen.LOG_IN.route -> false
                Screen.SPLASH_SCREEN.route -> false
                else -> true
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

            SUNTheme {
                CompositionLocalProvider(LocalSnackbar provides snackbarHostState) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
//                                    if (topBarVisibility) {
                                        TopBar(
                                            currentScreen = currentScreen,
                                            onProfileClick = {
                                                navController.navigate(Screen.PROFILE.route) {
                                                    popUpTo(Screen.HOME.route)
                                                }
                                            },
                                            onBackClick = { navController.popBackStack() }
                                        )
//                                    }
                                }
                            },
                        ) { paddingValues ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                            ) {
                                NavHost(
                                    navController = navController,
                                    startDestination = Screen.SPLASH_SCREEN.route
                                ) {
                                    composable(Screen.SPLASH_SCREEN.route) {
                                        SplashScreen()
                                    }
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
                                                navController.navigate(screen.route) {
                                                    launchSingleTop = true
                                                }
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
                        AnimatedVisibility(
                            visible = errorOverlayState.visible,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                                ErrorOverlay(
                                    iconId = errorOverlayState.icon,
                                    message = errorOverlayState.msg,
                                    resolve = {
                                        connectivityCheck()
                                    },
                                    reject = {
                                        errorOverlayState = OverlayDialogState()
                                    }
                                )
                        }
                    }
                }
            }
        }
    }

    // check internet and wifi availability
    private fun connectionAvailability(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (connectivityManager != null) {
            val networkCapabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (networkCapabilities != null) {
                return true
            }
        }
        return false
    }

    // ping the server for availability
    private fun checkAPIAvailability(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (connectivityManager != null) {
            return try {
                val url = URL(Config.API_BASE_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("Connection", "close");
                connection.connectTimeout = 10 * 1000;
                connection.connect();
                true
            } catch (e: Exception) {
                false
            }
        }
        return false
    }
}