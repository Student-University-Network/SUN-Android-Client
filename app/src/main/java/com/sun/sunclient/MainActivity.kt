package com.sun.sunclient

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import com.sun.sunclient.config.Config
import com.sun.sunclient.network.AppNotificationService
import com.sun.sunclient.ui.screens.course.CoursePage
import com.sun.sunclient.ui.screens.course.CoursesScreen
import com.sun.sunclient.ui.screens.home.HomeScreen
import com.sun.sunclient.ui.screens.login.LoginScreen
import com.sun.sunclient.ui.screens.profile.ProfileScreen
import com.sun.sunclient.ui.shared.ErrorOverlay
import com.sun.sunclient.ui.screens.splash.SplashScreen
import com.sun.sunclient.ui.screens.timetable.TimetableScreen
import com.sun.sunclient.ui.shared.TopBar
import com.sun.sunclient.ui.theme.SUNTheme
import com.sun.sunclient.utils.Screen
import com.sun.sunclient.utils.AppEvent
import com.sun.sunclient.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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

            var refreshing by remember { mutableStateOf(false) }
            fun refresh() {
                refreshing = true
                mainViewModel.syncData()
                coroutineScope.launch {
                    delay(10 * 1000)
                    refreshing = false
                }
            }

            val state = rememberPullRefreshState(refreshing, ::refresh)

            AppNotificationService.updateStatus.observe(this) {
                when (it) {
                    Constants.FETCH_TIMETABLE -> {
                        mainViewModel.syncTimetable()
                        AppNotificationService.updateStatus.value = ""
                    }
                    else -> {}
                }
            }

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
                            mainViewModel.setLoggedIn()
                        }
                        is AppEvent.OnLogout -> {
                            navigateAndClearStack(Screen.LOG_IN)
                        }
                        is AppEvent.PopBackStack -> {
                            navController.popBackStack()
                        }
                        is AppEvent.Navigate -> {
                            if (event.clearStack) {
                                navigateAndClearStack(event.route)
                            } else {
                                navController.navigate(event.route.route) {
                                    popUpTo(Screen.HOME.route)
                                }
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
                        is AppEvent.OnSyncedData -> {
                            refreshing = false
                        }
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
                val route = navController.currentDestination?.route ?: ""
                currentScreen = when (route) {
                    Screen.PROFILE.route -> Screen.PROFILE
                    Screen.COURSES.route -> Screen.COURSES
                    Screen.ANNOUCEMENTS.route -> Screen.ANNOUCEMENTS
                    Screen.ATTENDANCE.route -> Screen.ATTENDANCE
                    Screen.DISUCSSION.route -> Screen.DISUCSSION
                    Screen.TIMETABLE.route -> Screen.TIMETABLE
                    else -> Screen.HOME
                }
                val tmp = route.split("/")
                if (tmp.first() == "courses" && tmp.size >= 1) {
                    currentScreen = Screen.COURSES
                }
            }

            MultiplePermissionsHandler()
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
                            },
                        ) { paddingValues ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                                    .pullRefresh(state)
                            ) {
                                PullRefreshIndicator(
                                    refreshing, state,
                                    Modifier
                                        .align(Alignment.TopCenter)
                                        .zIndex(1f)
                                )

                                NavHost(
                                    navController = navController,
                                    startDestination = Screen.SPLASH_SCREEN.route
                                ) {
                                    composable(Screen.SPLASH_SCREEN.route) {
                                        SplashScreen()
                                    }
                                    composable(Screen.LOG_IN.route) {
                                        LoginScreen()
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
                                            onNavigateToTimetable = {
                                                navController.navigate(Screen.TIMETABLE.route) {
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
                                            mainViewModel = mainViewModel,
                                            navigateInScreen = { route ->
                                                navController.navigate(route) {
                                                    launchSingleTop = true
                                                }
                                            }
                                        )
                                    }
                                    composable(
                                        "${Screen.COURSES.route}/{courseId}", arguments = listOf(
                                            navArgument("courseId") { type = NavType.StringType })
                                    ) {
                                        CoursePage(
                                            it.arguments?.getString("courseId") ?: "",
                                            mainViewModel = mainViewModel
                                        )
                                    }
                                    composable(Screen.TIMETABLE.route) {
                                        TimetableScreen(mainViewModel = mainViewModel)
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
                connection.connectTimeout = 5 * 1000;
                connection.connect();
                true
            } catch (e: Exception) {
                false
            }
        }
        return false
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MultiplePermissionsHandler() {
    val context = LocalContext.current
    val permissionLists = ArrayList<String>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissionLists.add(Manifest.permission.POST_NOTIFICATIONS)
    }
    val permissionStates = rememberMultiplePermissionsState(permissions = permissionLists)
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    permissionStates.launchMultiplePermissionRequest()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    })


    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    )
    {
        permissionStates.permissions.forEach { it ->
            when (it.permission) {
                Manifest.permission.POST_NOTIFICATIONS -> {
                    when {
                        it.status.isGranted -> {
                            // None
                        }
                        it.status.shouldShowRationale -> {
                            // TODO : add rationale
                            Toast.makeText(
                                context,
                                "Please allow Push notifications for this app",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        !it.status.isGranted && !it.status.shouldShowRationale -> {
                            Toast.makeText(
                                context,
                                "Navigate to settings and enable the Storage permission",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }
}
