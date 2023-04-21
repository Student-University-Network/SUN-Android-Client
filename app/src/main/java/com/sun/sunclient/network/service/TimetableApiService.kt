package com.sun.sunclient.network.service

import com.sun.sunclient.network.schemas.GetTimetableResponse
import com.sun.sunclient.network.schemas.SetLectureInput
import com.sun.sunclient.network.schemas.SetLectureStatusResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TimetableApiService {

    @GET("timetable/faculty")
    suspend fun getFacultyTimetable() : GetTimetableResponse

    @GET("timetable/{batchId}")
    suspend fun getStudentTimetable(@Path("batchId") batchId: String) : GetTimetableResponse

    @POST("timetable/lecture-status")
    suspend fun setLectureStatus(@Body body: SetLectureInput) : SetLectureStatusResponse
}