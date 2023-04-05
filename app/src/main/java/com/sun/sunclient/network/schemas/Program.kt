package com.sun.sunclient.network.schemas

import java.util.*
import kotlin.collections.ArrayList

data class Batch(
    val id: String,
    val batchName: String,
    val students: Int,
)

data class Course(
    val courseId: String,
    val courseName: String,
    val totalLectures: Int,
    val compulsory: Boolean,
)

data class Semester(
    val semesterId: String,
    val semesterName: String,
    val order: Int,
    val courses: List<Course>,
)

data class Program(
    val programId: String = "",
    val programName: String = "",
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