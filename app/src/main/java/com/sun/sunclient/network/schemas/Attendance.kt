package com.sun.sunclient.network.schemas

data class TakeAttendanceInput(
    val lectureId: String,
    val courseId: String
)

data class TakeAttendanceResponse(
    val status: String,
    val data: TokenData
)

data class TokenData(
    val token: String
)

data class MarkAttendanceInput(
    val token: String
)

data class MarkAttendanceResponse(
    val status: String,
    val data: MarkedAttendanceData? = null
)

data class MarkedAttendanceData(
    val courseId: String = "",
    val lectureId: String = "",
    val userId: String,
    val result: String,
)

data class GetStudentReportResponse(
    val status: String,
    val data: StudentReport
)

data class StudentReport(
    val courses: List<ReportCourse>
)

data class ReportCourse(
    val courseId: String,
    val courseName: String,
    val compulsory: Boolean,
    val totalLectures: Int,
    val attended: Int,
)

data class GetFacultyReportResponse(
    val status: String,
    val data: FacultyReport
)

data class FacultyReport(
    val courseId: String,
    val courseName: String,
    val professorId: String,
    val professorName: String,
    val totalLectures: Int,
    val attendance: List<ReportStudent>
)

data class ReportStudent(
    val userId: String,
    val firstName: String,
    val lastName: String,
    val attended: Int
)