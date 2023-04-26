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