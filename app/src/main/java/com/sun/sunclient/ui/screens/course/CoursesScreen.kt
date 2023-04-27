package com.sun.sunclient.ui.screens.course

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.sun.sunclient.network.schemas.Course
import com.sun.sunclient.network.schemas.FacultyCourse
import com.sun.sunclient.network.schemas.Semester
import com.sun.sunclient.utils.Constants.ADMIN
import com.sun.sunclient.utils.Constants.FACULTY
import com.sun.sunclient.utils.Constants.STUDENT
import com.sun.sunclient.utils.Screen

@Composable
fun CoursesScreen(
    mainViewModel: MainViewModel,
    navigateInScreen: (route: String) -> Unit,
) {
    val TAG = "CoursesScreen"
    val currentSemester = mainViewModel.getCurrentSemester()
    val courses = currentSemester.courses

    if ((mainViewModel.userData.role == STUDENT && courses.isEmpty())
        || (mainViewModel.userData.role == FACULTY && mainViewModel.facultyCourses.isEmpty())
        || mainViewModel.userData.role == ADMIN
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.size(100.dp),
                painter = painterResource(id = R.drawable.im_empty_page),
                contentDescription = "No courses",
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            )
            Text(
                "No Courses are available right now..",
                fontSize = 18.sp,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(8.dp),
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (mainViewModel.userData.role == STUDENT) {
                items(
                    count = courses.size
                ) { index ->
                    CourseCard(
                        onCourseClick = { navigateInScreen("${Screen.COURSES.route}/${courses[index].courseId}") },
                        courseData = courses[index]
                    )
                }
            } else if (mainViewModel.userData.role == FACULTY) {
                items(
                    count = mainViewModel.facultyCourses.size
                ) { index ->
                    FacultyCourseCard(
                        onCourseClick = { navigateInScreen("${Screen.COURSES.route}/${mainViewModel.facultyCourses[index].courseId}") },
                        courseData = mainViewModel.facultyCourses[index],
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyCourseCard(
    modifier: Modifier = Modifier,
    courseData: FacultyCourse,
    onCourseClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(0.85f),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = { onCourseClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .size(60.dp)
                    .background(
                        MaterialTheme.colorScheme.onBackground,
                        shape = RoundedCornerShape(20)
                    )
                    .padding(8.dp)
            ) {
                Image(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50)),
                    painter = painterResource(id = R.drawable.im_subject_4),
                    contentDescription = "Subject logo",
                    contentScale = ContentScale.FillBounds,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surface)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    courseData.courseName,
                    modifier = Modifier.padding(vertical = 8.dp),
                    fontSize = 18.sp,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    courseData.programName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        courseData.batchName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("|")
                    Text(
                        courseData.semesterName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseCard(
    modifier: Modifier = Modifier,
    courseData: Course,
    onCourseClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(0.85f),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = { onCourseClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .size(60.dp)
                    .background(
                        MaterialTheme.colorScheme.onBackground,
                        shape = RoundedCornerShape(20)
                    )
                    .padding(8.dp)
            ) {
                Image(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50)),
                    painter = painterResource(id = R.drawable.im_subject_4),
                    contentDescription = "Subject logo",
                    contentScale = ContentScale.FillBounds,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surface)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    courseData.courseName,
                    fontSize = 18.sp,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(50)),
                        painter = painterResource(id = R.drawable.ic_profile),
                        contentDescription = "Professor profile image",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                    )
                    Text(
                        "Prof. ${courseData.professorName ?: "Unknown"}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}