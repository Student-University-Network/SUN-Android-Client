package com.sun.sunclient.ui.screens.profile

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.sun.sunclient.MainViewModel

@Composable
fun ProfileScreen(
    mainViewModel: MainViewModel
) {
    Text("Profile Screen")
    Button(onClick = { mainViewModel.logout() }) {
        Text("Log out")
    }
}