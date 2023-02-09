package com.sun.sunclient.ui.screens.course

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
import com.sun.sunclient.R
import com.sun.sunclient.utils.Screen

@Composable
fun CoursesScreen(
    navigateInScreen: (screen: Screen) -> Unit,
) {

    // TODO : use courses data for empty condition
    if (false) {
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
                "No Courses are avaiable right now..",
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
            // TODO: use items{} to iterate through courses and replace placholders
            item {
                CourseCard(onCourseClick = { navigateInScreen(Screen.COURSEPAGE) })
            }
            item {
                CourseCard()
            }
            item {
                CourseCard()
            }
            item {
                CourseCard()
            }
            item {
                CourseCard()
            }
            item {
                CourseCard()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseCard(
    modifier: Modifier = Modifier,
    onCourseClick: () -> Unit = {}
) {
    // TODO: use course data here instead of placeholders
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
                    "AIDS-II",
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
                        "Prof. Yash Sawant",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text("4 new posts", fontSize = 12.sp, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}