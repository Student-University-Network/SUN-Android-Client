package com.sun.sunclient.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sun.sunclient.R
import com.sun.sunclient.config.University

@Composable
fun SplashScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF3D7DDC)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val logoRotateTransition = rememberInfiniteTransition()
        val angle by logoRotateTransition.animateFloat(
            initialValue = 0F,
            targetValue = 90F,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOut)
            )
        )
        Image(
            modifier = Modifier
                .padding(16.dp)
                .size(200.dp)
                .graphicsLayer { rotationZ = angle },
            painter = painterResource(
                id = R.drawable.ic_app_icon
            ),
            contentDescription = "App Logo",
        )
        Text(University.nickname, fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Color.White)
        LinearProgressIndicator(
            modifier = Modifier.padding(vertical = 32.dp),
            color = Color(0xFF71A5F1),
            trackColor = Color.White
        )
    }
}