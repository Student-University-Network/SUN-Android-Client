package com.sun.sunclient.application

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel {
    var loggedIn by mutableStateOf(false)
    private set

    constructor():super() {

    }

    fun logIn() {
        loggedIn = true
    }

    fun logOut() {
        loggedIn = false
    }
}