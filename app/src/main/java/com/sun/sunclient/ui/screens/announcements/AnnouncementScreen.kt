package com.sun.sunclient.ui.screens.announcements

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sun.sunclient.R


data class TabItem(val name: String)

@Composable
fun AnnouncementScreen(

) {
    var currentTab by remember { mutableStateOf(0) }
    val tabItems = listOf<TabItem>(
        TabItem("Courses"),
        TabItem("Programs"),
    )

    val roundedCorner = RoundedCornerShape(50)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            TabRow(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(roundedCorner)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        shape = roundedCorner
                    ),
                selectedTabIndex = currentTab,
                containerColor = MaterialTheme.colorScheme.surface,
                indicator = { tabPosition ->
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPosition[currentTab])
                            .fillMaxHeight()
                            .background(Color(0x2F596EFF), shape = roundedCorner)
                    )
                },
                divider = { }
            ) {
                tabItems.forEachIndexed { index, tabItem ->
                    Tab(
                        selected = currentTab == index,
                        onClick = { currentTab = index }
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                                .clip(roundedCorner),
                            text = tabItem.name, fontSize = 14.sp
                        )
                    }
                }
            }
        }
        item {
            ItemsList(currentTab = currentTab)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ItemsList(
    currentTab: Int
) {
    AnimatedContent(targetState = currentTab,
        transitionSpec = {
            fadeIn() + slideInVertically(
                animationSpec = tween(400),
                initialOffsetY = { fullHeight -> -fullHeight }
            ) with fadeOut(animationSpec = tween(200))
        }) { currentTabState ->
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(MaterialTheme.colorScheme.surface, shape = ShapeDefaults.Medium)
                .clip(ShapeDefaults.Medium)
        ) {
            when (currentTabState) {
                // TODO: iterate through data
                0 -> for (i in 1..20) {
                    CourseNotificationCards()
                }
                1 -> for (i in 1..20) {
                    ProgramNotificationsCards()
                }
            }
        }
    }
}

// TODO: complete all Item Composabales
@Composable
fun CourseNotificationCards() {
    Text("Course Notifications")
}

@Composable
fun ProgramNotificationsCards() {
    Text("Programs Notifications")
}

