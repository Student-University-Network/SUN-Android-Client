package com.sun.sunclient.application

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel {

    // TODO : set loggedIn to true until dev
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