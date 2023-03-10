package com.sun.sunclient.ui.screens.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sun.sunclient.R
import com.sun.sunclient.MainViewModel
import java.util.*

@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToCourses: () -> Unit,
    mainViewModel: MainViewModel
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(
            vertical = 8.dp,
            horizontal = 18.dp
        ),
        verticalArrangement = Arrangement.spacedBy(26.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            // header
            Card(
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // TODO: replace placeholder values with user details
                    Text("Manas Raut", fontSize = 22.sp)
                    Text("BEIT-2 | Roll no. 36", fontSize = 14.sp)
                    Text("2022-2023", fontSize = 14.sp)
                }
            }
        }
        item {
            ServicesCard(
                name = "Courses",
                icon = R.drawable.ic_course,
                onServiceClick = { onNavigateToCourses() })
        }
        item {
            ServicesCard(
                name = "Timetable",
                icon = R.drawable.ic_timetable,
                onServiceClick = { })
        }
        item {
            ServicesCard(
                name = "Attendance",
                icon = R.drawable.ic_attendance,
                onServiceClick = { })
        }
        item {
            ServicesCard(
                name = "Chat",
                icon = R.drawable.ic_chat,
                onServiceClick = { })
        }
        item {
            ServicesCard(
                name = "Notices",
                icon = R.drawable.ic_bell,
                onServiceClick = { })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesCard(
    modifier: Modifier = Modifier,
    name: String,
    icon: Int,
    onServiceClick: () -> Unit
) {
    Card(
        onClick = { onServiceClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // TODO: replace placeholder service
                Image(
                    modifier = Modifier
                        .padding(12.dp)
                        .size(38.dp),
                    painter = painterResource(id = icon),
                    contentDescription = name,
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                )
                Text(name, fontSize = 16.sp)
            }
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(50)
                    )
                    .padding(vertical = 2.dp, horizontal = 8.dp)
                    .align(Alignment.TopEnd)
            ) {
                Text(
                    "2",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}