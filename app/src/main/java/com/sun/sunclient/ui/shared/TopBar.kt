package com.sun.sunclient.ui.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sun.sunclient.R
import com.sun.sunclient.utils.Screen

@Composable
fun TopBar(
    currentScreen: Screen,
    onProfileClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { if (currentScreen != Screen.HOME) onBackClick() }) {
                    if (currentScreen == Screen.HOME) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = currentScreen.icon),
                            contentDescription = currentScreen.name,
                        )
                    } else {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Go Back")
                    }
                }
                Text(text = currentScreen.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = { if (currentScreen != Screen.PROFILE) onProfileClick() }) {
                // TODO: replace placeholder image with profile image
                Image(
                    painter = painterResource(id = R.drawable.ic_profile),
                    contentDescription = "Profile image",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                )
            }
        }
    }
}