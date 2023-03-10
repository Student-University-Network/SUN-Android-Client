package com.sun.sunclient

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.sun.sunclient.config.Config
import com.sun.sunclient.ui.screens.error.ErrorOverlay
import com.sun.sunclient.ui.screens.splash.SplashScreen
import com.sun.sunclient.ui.theme.SUNTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // viewModels
            val mainViewModel: MainViewModel = hiltViewModel()

            // state
            var errorOverlayVisible by remember { mutableStateOf(false) }
            var errorMsg by remember { mutableStateOf("Oops!!, something went wrong") }
            var errorIcon by remember { mutableStateOf(R.drawable.im_empty_page) }

            // do network checks
            LaunchedEffect(key1 = true) {
                val coroutineScope = CoroutineScope(Dispatchers.IO)
                coroutineScope.launch {
                    errorOverlayVisible = false
                    if (!checkConnectivity()) {
                        Log.i(TAG, "No connectivity available")
                        errorOverlayVisible = true
                        errorMsg = "Internet not available"
                        errorIcon = R.drawable.ic_no_connection
                        return@launch
                    }
                    if (!checkAPIAvailability()) {
                        Log.i(TAG, "Server not available")
                        errorOverlayVisible = true
                        errorMsg = "Server not available. Try again later"
                        errorIcon = R.drawable.ic_server_down
                        return@launch
                    }
                }
            }

            SUNTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AnimatedContent(
                        targetState = mainViewModel.showSplashScreen,
                    ) {
                        if (!mainViewModel.showSplashScreen) {
                            SplashScreen()
                        } else {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AppNavigation(mainViewModel = mainViewModel)
                                AnimatedVisibility(visible = errorOverlayVisible) {
                                    if (errorOverlayVisible) {
                                        ErrorOverlay(iconId = errorIcon, message = errorMsg)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // check internet and wifi availability
    private fun checkConnectivity(): Boolean {
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

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SUNTheme {
    }
}