package com.sun.sunclient.utils

import com.sun.sunclient.R

sealed class Screen(
    val route: String,
    val name: String,
    val icon: Int
) {
    object HOME : Screen("home", "Home", R.drawable.ic_dashboard)
    object PROFILE : Screen("profile", "Profile", R.drawable.ic_dashboard)
    object COURSES : Screen("courses", "Courses", R.drawable.ic_courses)
    object COURSEPAGE: Screen("courses/coursepage", "Courses", R.drawable.ic_courses)
    object ATTENDANCE : Screen("attendance", "Attendance", R.drawable.ic_courses)
    object TIMETABLE : Screen("timetable", "Timetable", R.drawable.ic_courses)
    object ANNOUNCEMENTS : Screen("announcements", "Announcements", R.drawable.ic_announcements)
    object DISUCSSION : Screen("discussion", "Discussion", R.drawable.ic_courses)
}