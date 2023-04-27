package com.sun.sunclient.ui.screens.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.WorkManager
import com.sun.sunclient.R
import com.sun.sunclient.MainViewModel
import com.sun.sunclient.network.background.AttendanceWorker
import com.sun.sunclient.network.schemas.Lecture
import com.sun.sunclient.ui.screens.timetable.getLectureTimeString
import com.sun.sunclient.utils.Constants
import com.sun.sunclient.utils.Constants.ADMIN
import com.sun.sunclient.utils.Constants.STUDENT
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToCourses: () -> Unit,
    onNavigateToTimetable: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToAnnouncements: () -> Unit,
    mainViewModel: MainViewModel
) {

    val context = LocalContext.current
    val role = mainViewModel.userData.role
    var currentLecture by remember { mutableStateOf<Lecture?>(null) }

    fun doAttendance() {
        currentLecture?.let {
            AttendanceWorker.takeALectureAttendance(context, it.id, it.courseId, role)
        }
    }

    LaunchedEffect(key1 = true) {
        val timetable = mainViewModel.timetableData
        val today = timetable.days.find { d ->
            d.weekDay == (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1)
        }
        today?.let { d ->
            val currentTime = System.currentTimeMillis()
            for (l in d.lectures) {
                val startTime = Calendar.getInstance().apply {
                    this.set(Calendar.HOUR_OF_DAY, l.startTime.hour)
                    this.set(Calendar.MINUTE, l.startTime.minute)
                    this.set(Calendar.SECOND, 0)
                }.timeInMillis
                val endTime = Calendar.getInstance().apply {
                    this.set(Calendar.HOUR_OF_DAY, l.endTime.hour)
                    this.set(Calendar.MINUTE, l.endTime.minute)
                    this.set(Calendar.SECOND, 0)
                }.timeInMillis
                if (currentTime in startTime until endTime) {
                    currentLecture = l
                    return@LaunchedEffect
                }
            }
        }
    }

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
                    Text(
                        "${mainViewModel.userProfile.firstName} ${mainViewModel.userProfile.lastName}",
                        fontSize = 22.sp
                    )
                    if (mainViewModel.userData.role == ADMIN) {
                        Pill(label = "Admin")
                    } else if (mainViewModel.userData.role == STUDENT) {
                        Text(
                            "${mainViewModel.programData.programName} " +
                                    "|" +
                                    " ${
                                        mainViewModel.programData.batches.find {
                                            it.id == mainViewModel.programData.batchId
                                        }?.batchName
                                    }",
                            fontSize = 14.sp
                        )
                        Text(
                            "${SimpleDateFormat("yyyy").format(mainViewModel.programData.startYear)}-${
                                SimpleDateFormat(
                                    "yyyy"
                                ).format(mainViewModel.programData.endYear)
                            }", fontSize = 14.sp
                        )
                    } else {
                        Pill(label = "Faculty")
                    }
                }
            }
        }
        item(span = { GridItemSpan(2) }) {
            Card(
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    currentLecture?.let { lecture ->
                        Text("Current lecture")
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                lecture.courseName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            OutlinedButton(onClick = { doAttendance() }) {
                                if (role == Constants.FACULTY) {
                                    Text("Take attendance")
                                } else {
                                    Text("Mark attendance")
                                }
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_time),
                                contentDescription = "Clock icon",
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                            )
                            Text(getLectureTimeString(lecture.startTime, lecture.endTime))
                        }
                    }
                    if (currentLecture == null) {
                        Text("No ongoing lecture", fontSize = 14.sp)
                    }
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
                onServiceClick = { onNavigateToTimetable() })
        }
        item {
            ServicesCard(
                name = "Attendance",
                icon = R.drawable.ic_attendance,
                onServiceClick = { onNavigateToAttendance() })
        }
        item {
            ServicesCard(
                name = "Notices",
                icon = R.drawable.ic_bell,
                onServiceClick = { onNavigateToAnnouncements() })
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
//            Box(
//                modifier = Modifier
//                    .padding(4.dp)
//                    .background(
//                        MaterialTheme.colorScheme.primary,
//                        shape = RoundedCornerShape(50)
//                    )
//                    .padding(vertical = 2.dp, horizontal = 8.dp)
//                    .align(Alignment.TopEnd)
//            ) {
//                Text(
//                    "2",
//                    fontSize = 14.sp,
//                    color = MaterialTheme.colorScheme.onPrimary
//                )
//            }
        }
    }
}

@Composable
fun Pill(label: String) {
    Text(
        label,
        fontSize = 14.sp,
        modifier = Modifier
            .padding(vertical = 4.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = ShapeDefaults.Large
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    )
}