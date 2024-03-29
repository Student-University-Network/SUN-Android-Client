package com.sun.sunclient.ui.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sun.sunclient.R

@Composable
fun ErrorOverlay(
    iconId: Int,
    message: String,
    showResolve: Boolean = true,
    showReject: Boolean = true,
    resolve: () -> Unit,
    reject: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .shadow(6.dp, shape = RoundedCornerShape(12.dp))
                    .background(
                        MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Image(
                    modifier = Modifier.size(64.dp),
                    painter = painterResource(id = iconId),
                    contentDescription = "Error icon",
                    colorFilter = ColorFilter.tint(Color(0xFF3F3F3F))
                )
                Text(message, fontSize = 18.sp, textAlign = TextAlign.Center)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showResolve) {
                        Button(onClick = { resolve() }) {
                            Text("Try again")
                        }
                    }
                    if (showReject) {
                        OutlinedButton(
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                            onClick = { reject() }) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}