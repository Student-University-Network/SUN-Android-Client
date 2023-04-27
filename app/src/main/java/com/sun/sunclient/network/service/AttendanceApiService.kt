package com.sun.sunclient.network.service

import com.sun.sunclient.network.schemas.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AttendanceApiService {

    @POST("attendance/start")
    suspend fun takeAttendance(@Body body: TakeAttendanceInput): TakeAttendanceResponse

    @POST("attendance/mark")
    suspend fun markAttendance(@Body body: MarkAttendanceInput): MarkAttendanceResponse

    @GET("attendance/report")
    suspend fun getStudentReport(
        @Query("courseId") courseId: String = "",
        @Query("batchId") batchId: String = ""
    ): GetStudentReportResponse

    @GET("attendance/report")
    suspend fun getFacultyReport(
        @Query("courseId") courseId: String,
        @Query("batchId") batchId: String
    ): GetFacultyReportResponse
}