package com.sun.sunclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sun.sunclient.application.MainViewModel
import com.sun.sunclient.ui.screens.login.LoginScreen
import com.sun.sunclient.ui.screens.splash.SplashScreen
import com.sun.sunclient.ui.theme.SUNTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val mainViewModel: MainViewModel = viewModel()

            SUNTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AnimatedContent(
                        targetState = mainViewModel.onStartLoaded,
                    ) {
                        if (!mainViewModel.onStartLoaded) {
                            SplashScreen()
                        } else if (!mainViewModel.loggedIn) {
                            LoginScreen()
                        } else {
                            AppNavigation(
                                mainViewModel = mainViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SUNTheme {
    }
}