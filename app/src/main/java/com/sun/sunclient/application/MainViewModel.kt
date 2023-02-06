package com.sun.sunclient.application

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.util.*

class MainViewModel: ViewModel {

    // TODO : set loggedIn to true until dev
    var loggedIn by mutableStateOf(false)
    private set

    var onStartLoaded by mutableStateOf(false)
    private set

    constructor():super() {
        onStart()
    }

    fun onStart() {
        // TODO: do all data fetching on application start here
        // Temporary added a delay so splashscreen can be seen
        // remove it later
        Timer().schedule(object : TimerTask() {
            override fun run() {
                onStartLoaded = true
            }
        }, 2000)
    }

    fun logIn() {
        loggedIn = true
    }

    fun logOut() {
        loggedIn = false
    }
}