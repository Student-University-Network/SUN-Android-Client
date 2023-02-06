package com.sun.sunclient.ui.screens.course

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TabItem(val name: String)

@Composable
fun CoursePage(

) {
    var currentTab by remember { mutableStateOf(0) }
    val tabItems = listOf<TabItem>(
        TabItem("Resources"),
        TabItem("Assignments"),
        TabItem("Links")
    )

    val roundedCorner = RoundedCornerShape(50)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            // TODO: replace placeholder data with course data
            Card(
                modifier = Modifier.fillMaxWidth(0.9f),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("AIDS-II", fontSize = 22.sp, overflow = TextOverflow.Ellipsis)
                    Text(
                        "Prof. Yash Sawant",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("4 new posts", fontSize = 12.sp, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        item {
            TabRow(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(roundedCorner),
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
                    ResourceItem()
                }
                1 -> for (i in 1..20) {
                    AssignmentsItem()
                }
                2 -> for (i in 1..20) {
                    LinksItem()
                }
            }
        }
    }
}

// TODO: complete all Item Composabales
@Composable
fun ResourceItem() {
    Text("Resource")
}

@Composable
fun AssignmentsItem() {
    Text("AssignmentsItem")
}

@Composable
fun LinksItem() {
    Text("LinksItem")
}