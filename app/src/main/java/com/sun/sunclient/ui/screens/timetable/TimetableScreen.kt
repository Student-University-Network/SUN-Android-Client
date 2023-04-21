package com.sun.sunclient.ui.screens.timetable

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sun.sunclient.MainViewModel
import com.sun.sunclient.R
import com.sun.sunclient.network.schemas.*
import com.sun.sunclient.ui.screens.course.TabItem
import com.sun.sunclient.ui.theme.errorColor
import com.sun.sunclient.ui.theme.successColor
import com.sun.sunclient.utils.Constants

@Composable
fun TimetableScreen(
    mainViewModel: MainViewModel
) {
    var currentTab by remember { mutableStateOf(0) }
    val tabItems = WeekDay.values().map() { day -> TabItem(day.toString()) }
    var selectedDay by remember { mutableStateOf(WeekDay.Sunday) }

    val timetable = mainViewModel.timetableData

    LaunchedEffect(key1 = true) {
        if (mainViewModel.timetableData.batchId == "") {
            mainViewModel.syncTimetable()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScrollableTabRow(
                modifier = Modifier
                    .fillMaxWidth(0.9f),
                selectedTabIndex = currentTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                tabItems.forEachIndexed { index, tabItem ->
                    Tab(
                        selected = currentTab == index,
                        onClick = {
                            currentTab = index
                            selectedDay = WeekDay[index] ?: WeekDay.Sunday
                        }
                    ) {
                        Text(tabItem.name, modifier = Modifier.padding(vertical = 18.dp))
                    }
                }
            }
            TabContent(
                currentTab = currentTab,
                timetable = timetable,
                selectedDay = selectedDay,
                userRole = mainViewModel.userData.role,
                mainViewModel = mainViewModel
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun TabContent(
    currentTab: Int,
    timetable: Timetable,
    selectedDay: WeekDay,
    userRole: String,
    mainViewModel: MainViewModel
) {
    val day: Day =
        timetable.days.find { it.weekDay == selectedDay.dayOfWeek } ?: Day(0, ArrayList())
    var currentLecture by remember { mutableStateOf("") }

    LaunchedEffect(key1 = true) {
        val firstLecture = day.lectures.find { it.status == LectureStatus.SCHEDULED }
        firstLecture?.let {
            currentLecture = firstLecture.id
        }
    }

    AnimatedContent(targetState = currentTab,
        transitionSpec = {
            fadeIn() + slideInVertically(
                animationSpec = tween(400),
                initialOffsetY = { fullHeight -> -fullHeight }
            ) with fadeOut(animationSpec = tween(200))
        }) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(vertical = 18.dp)
        ) {
            items(day.lectures) {
                TimetableCard(
                    it,
                    userRole = userRole,
                    mainViewModel = mainViewModel,
                    isCurrentLecture = currentLecture == it.id
                )
            }
            if (day.lectures.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            modifier = Modifier.size(70.dp),
                            painter = painterResource(id = R.drawable.ic_empty_calender),
                            contentDescription = "No lectures today",
                            contentScale = ContentScale.Fit,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            "There are no lectures today",
                            fontSize = 18.sp,
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

fun getLectureTimeString(start: LectureTime, end: LectureTime): String {
    var res: String = if (start.hour < 10) {
        "0${start.hour}"
    } else {
        "${start.hour}"
    } + ":"
    res += if (start.minute < 10) {
        "0${start.minute}"
    } else {
        "${start.minute}"
    } + "-"
    res += if (end.hour < 10) {
        "0${end.hour}"
    } else {
        "${end.hour}"
    } + ":"
    res += if (end.minute < 10) {
        "0${end.minute}"
    } else {
        "${end.minute}"
    }
    return res
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimetableCard(
    lecture: Lecture,
    userRole: String,
    mainViewModel: MainViewModel,
    isCurrentLecture: Boolean
) {

    var expanded by remember { mutableStateOf(false) }
    var lectureStatus by remember { mutableStateOf(lecture.status) }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    LaunchedEffect(key1 = lectureStatus) {
        mainViewModel.setLectureStatus(lecture.batchId ?: "", lecture.id, lectureStatus)
    }

    LaunchedEffect(key1 = isCurrentLecture) {
        bringIntoViewRequester.bringIntoView()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .bringIntoViewRequester(bringIntoViewRequester),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (lecture.status == LectureStatus.CANCELLED) Modifier.background(
                        MaterialTheme.colorScheme.surfaceVariant
                    ) else Modifier.background(MaterialTheme.colorScheme.surface)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (lecture.status) {
                        LectureStatus.COMPLETED -> {
                            Text("Completed", color = successColor, fontSize = 12.sp)
                        }
                        LectureStatus.CANCELLED -> {
                            Text("Cancelled", color = errorColor, fontSize = 12.sp)
                        }
                        else -> {
                            Text(
                                "Scheduled",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                    if (userRole == Constants.FACULTY && lecture.status != LectureStatus.COMPLETED) {
                        Box() {
                            IconButton(modifier = Modifier.size(14.dp),
                                onClick = { expanded = !expanded }) {
                                Icon(
                                    modifier = Modifier.size(14.dp),
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More"
                                )
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                            ) {
                                if (lecture.status == LectureStatus.SCHEDULED) {
                                    DropdownMenuItem(
                                        text = {
                                            Text("Cancel")
                                        },
                                        onClick = {
                                            lectureStatus = LectureStatus.CANCELLED
                                            expanded = false
                                        })
                                    DropdownMenuItem(
                                        text = { Text("Complete") },
                                        onClick = {
                                            lectureStatus = LectureStatus.COMPLETED
                                            expanded = false
                                        })
                                } else if (lecture.status == LectureStatus.CANCELLED) {
                                    DropdownMenuItem(
                                        text = { Text("Reschedule") },
                                        onClick = {
                                            lectureStatus = LectureStatus.SCHEDULED
                                            expanded = false
                                        })
                                }
                            }
                        }
                    }
                }
                Text(
                    lecture.courseName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Image(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(50)),
                        painter = painterResource(id = R.drawable.ic_profile),
                        contentDescription = "Professor icon",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                    )
                    Text(
                        text = if (userRole == Constants.FACULTY) {
                            lecture.batchName ?: "no batch"
                        } else {
                            "Prof. ${lecture.professorName}"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Image(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(50)),
                        painter = painterResource(id = R.drawable.ic_time),
                        contentDescription = "Clock icon",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                    )
                    Text(
                        getLectureTimeString(lecture.startTime, lecture.endTime),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Image(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(50)),
                        painter = painterResource(id = R.drawable.ic_home),
                        contentDescription = "Room icon",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                    )
                    Text(
                        lecture.room,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}