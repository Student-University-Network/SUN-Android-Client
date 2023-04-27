package com.sun.sunclient.ui.screens.attendance

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sun.sunclient.MainViewModel
import com.sun.sunclient.R
import com.sun.sunclient.network.schemas.FacultyCourse
import com.sun.sunclient.network.schemas.FacultyReport
import com.sun.sunclient.network.schemas.LectureStatus
import com.sun.sunclient.network.schemas.StudentReport
import com.sun.sunclient.ui.screens.course.FacultyCourseCard
import com.sun.sunclient.utils.Constants
import com.sun.sunclient.utils.Screen

@Composable
fun AttendanceScreen(mainViewModel: MainViewModel, navigateInScreen: (route: String) -> Unit) {
    val role = mainViewModel.userData.role
    val attendanceViewModel: AttendanceViewModel = hiltViewModel()
    val coursesList = mainViewModel.facultyCourses

    LaunchedEffect(key1 = true) {
        if (role == Constants.STUDENT) {
            attendanceViewModel.getReport(role, "", "")
        }
    }

    if (role == Constants.FACULTY) {
        FacultyAttendanceScreen(
            coursesList,
            attendanceViewModel.facultyAttendanceReport,
            navigateInScreen
        )
    } else if (role == Constants.STUDENT && attendanceViewModel.studentAttendanceReport != null) {
        attendanceViewModel.studentAttendanceReport?.let { StudentAttendanceScreen(it) }
    } else {
        NoReportMsg()
    }
}

@Composable
fun FacultyAttendanceScreen(
    courses: List<FacultyCourse>,
    report: FacultyReport?,
    navigateInScreen: (route: String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(vertical = 18.dp)
    ) {
        items(courses) {
            FacultyCourseCard(
                courseData = it,
                onCourseClick = { navigateInScreen("${Screen.ATTENDANCE.route}/${it.batchId}/${it.courseId}") })
        }
    }
}

@Composable
fun FacultyAttendanceCoursePage(
    courseId: String,
    batchId: String,
) {
    val attendanceViewModel: AttendanceViewModel = hiltViewModel()
    val report = attendanceViewModel.facultyAttendanceReport

    LaunchedEffect(key1 = true) {
        attendanceViewModel.getReport(Constants.FACULTY, courseId, batchId)
    }

    if (report?.courseName != null && report?.totalLectures != null && report?.attendance != null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                report.courseName,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 36.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 36.dp)
            ) {
                Text("Lectures: ${report.totalLectures}")
                Text("Students: ${report.attendance.size}")
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(report.attendance) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        elevation = CardDefaults.cardElevation(2.dp),
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
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "${it.firstName} ${it.lastName}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Text("Attended: ${it.attended}")
                            if (report.totalLectures == 0) {
                                Text(
                                    "Total attendance: 0%",
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            } else {
                                Text(
                                    "Total attendance: ${it.attended / (report.totalLectures * 1.0f) * 100}%",
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        NoReportMsg()
    }
}

@Composable
fun StudentAttendanceScreen(report: StudentReport) {
    val courses = report.courses

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(vertical = 18.dp)
    ) {
        items(courses) {
            Card(
                modifier = Modifier.fillMaxWidth(0.85f),
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
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        it.courseName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Lectures: ${it.totalLectures}")
                        Text("Attended: ${it.attended}")
                    }
                    if (it.totalLectures == 0) {
                        Text("Total attendance: 0%", modifier = Modifier.padding(vertical = 4.dp))
                    } else {
                        Text(
                            "Total attendance: ${it.attended / (it.totalLectures * 1.0f) * 100}%",
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoReportMsg() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.size(70.dp),
            painter = painterResource(id = R.drawable.im_empty_page),
            contentDescription = "No present report for attendance",
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
        )
        Text(
            "No present report for attendance",
            fontSize = 18.sp,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(8.dp),
            textAlign = TextAlign.Center
        )
    }
}