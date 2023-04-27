package com.sun.sunclient.network.schemas

import java.util.*
import kotlin.collections.ArrayList

data class Batch(
    val id: String = "",
    val batchName: String = "Unknown",
    val students: Int = 0,
)

data class Course(
    val courseId: String  = "",
    val courseName: String = "Unknown",
    val totalLectures: Int = 0,
    val compulsory: Boolean = true,
    val professorId: String? = null,
    val professorName: String? = null,
)

data class Semester(
    val semesterId: String = "",
    val semesterName: String = "Unknown",
    val order: Int = 0,
    val courses: List<Course> = ArrayList(),
)

data class Program(
    val programId: String = "",
    val programName: String = "Unknown",
    val duration: Int = 0,
    val startYear: Date = Date(),
    val endYear: Date= Date(),
    val tag: String = "",
    val batches: List<Batch> = ArrayList(),
    val currentSemester: Int = 0,
    val semesters: List<Semester> = ArrayList(),
    val batchId: String = "",
)

data class GetProgramDetailsResponse(
    val status: String,
    val data: Program = Program(),
)

data class GetFacultyCoursesResponse(
    val status: String,
    val data : Data = Data()
) {
    data class Data(
        val courses: List<FacultyCourse> = ArrayList()
    )
}

data class FacultyCourse(
    val courseId: String = "",
    val courseName: String = "Unknown",
    val totalLectures: Int = 0,
    val compulsory: Boolean = true,
    val semesterId: String = "",
    val semesterName: String = "",
    val programId: String = "",
    val programName: String = "",
    val batchId: String = "",
    val batchName: String = ""
)
