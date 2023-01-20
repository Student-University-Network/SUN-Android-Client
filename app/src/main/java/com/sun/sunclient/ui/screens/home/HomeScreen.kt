package com.sun.sunclient.ui.screens.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sun.sunclient.R
import com.sun.sunclient.application.MainViewModel

@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToCourses: () -> Unit,
    mainViewModel: MainViewModel
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(top = 16.dp, bottom = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Card(modifier = Modifier.fillMaxWidth(0.8f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // TODO: replace placeholder values with user details
                        Text("Manas Raut", fontSize = 26.sp)
                        Text("BEIT-2 | Roll no. 36")
                        Text("2022-2023")
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .padding(vertical = 32.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TODO: add ServiceCard for each service
            ServicesCard(onServiceClick = { onNavigateToCourses() })
            ServicesCard()
            ServicesCard()
            ServicesCard()
            ServicesCard()
            ServicesCard()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesCard(
    modifier: Modifier = Modifier,
    onServiceClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(0.8f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = { onServiceClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // TODO: replace placeholder service
            Image(
                modifier = Modifier.padding(12.dp).size(32.dp),
                painter = painterResource(id = R.drawable.ic_courses),
                contentDescription = "Courses",
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            )
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Text("Courses", fontSize = 18.sp)
                Text("New material added", fontSize = 12.sp, fontWeight = FontWeight.Light)
            }
        }
    }
}