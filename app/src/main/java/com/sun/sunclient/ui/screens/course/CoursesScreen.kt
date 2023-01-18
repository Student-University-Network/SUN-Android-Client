package com.sun.sunclient.ui.screens.course

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sun.sunclient.application.MainViewModel

@Composable
fun CoursesScreen(
    modifier: Modifier = Modifier,
    onNavigateToHome: () -> Unit,
    mainViewModel: MainViewModel
) {
    Text("Courses")
}