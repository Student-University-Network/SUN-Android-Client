package com.sun.sunclient.network.schemas

data class GetTimetableResponse(
    val status: String,
    val data: Timetable? = null
)

data class Timetable(
    val batchId: String = "",
    val batchName: String = "",
    val days: List<Day> = ArrayList(),
)

data class Day(
    val weekDay: Int,
    val lectures: List<Lecture>,
)

data class Lecture(
    val id: String,
    val courseId: String,
    val courseName: String,
    val professorId: String,
    val professorName: String,
    val room: String,
    val startTime: LectureTime,
    val endTime: LectureTime,
    val batchId: String?,
    val batchName: String?,
    val status: LectureStatus,
)

data class LectureTime(
    val hour: Int,
    val minute: Int,
)

data class SetLectureInput(
    val batchId: String,
    val lectureId: String,
    val status: LectureStatus
)

data class SetLectureStatusResponse(
    val status: String,
)

enum class LectureStatus {
    SCHEDULED,
    COMPLETED,
    CANCELLED,
}

enum class WeekDay(val dayOfWeek: Int) {
    Sunday(0),
    Monday(1),
    Tuesday(2),
    Wednesday(3),
    Thursday(4),
    Friday(5),
    Saturday(6);

    companion object {
        private val map = WeekDay.values().associateBy { it.dayOfWeek }
        operator fun get(value: Int) = map[value]
    }
}