package com.sun.sunclient.utils

import com.sun.sunclient.R

sealed class Screen(
    val route: String,
    val name: String,
    val icon: Int
) {
    object LOG_IN : Screen("login", "Log in", R.drawable.ic_app_icon)
    object HOME : Screen("home", "Home", R.drawable.ic_app_icon)
    object PROFILE : Screen("profile", "Profile", R.drawable.ic_dashboard)
    object COURSES : Screen("courses", "Courses", R.drawable.ic_course)
    object COURSEPAGE: Screen("courses/coursepage", "Courses", R.drawable.ic_course)
    object ATTENDANCE : Screen("attendance", "Attendance", R.drawable.ic_attendance)
    object TIMETABLE : Screen("timetable", "Timetable", R.drawable.ic_timetable)
    object ANNOUCEMENTS : Screen("announcements", "Announcements", R.drawable.ic_announcements)
    object DISUCSSION : Screen("discussion", "Discussion", R.drawable.ic_chat)
}