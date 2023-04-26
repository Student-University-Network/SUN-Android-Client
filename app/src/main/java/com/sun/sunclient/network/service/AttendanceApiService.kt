package com.sun.sunclient.network.service

import com.sun.sunclient.network.schemas.MarkAttendanceInput
import com.sun.sunclient.network.schemas.MarkAttendanceResponse
import com.sun.sunclient.network.schemas.TakeAttendanceInput
import com.sun.sunclient.network.schemas.TakeAttendanceResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AttendanceApiService {

    @POST("attendance/start")
    suspend fun takeAttendance(@Body body: TakeAttendanceInput) : TakeAttendanceResponse

    @POST("attendance/mark")
    suspend fun markAttendance(@Body body: MarkAttendanceInput) : MarkAttendanceResponse
}