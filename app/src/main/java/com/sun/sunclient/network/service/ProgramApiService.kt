package com.sun.sunclient.network.service

import com.sun.sunclient.network.schemas.GetFacultyCoursesResponse
import com.sun.sunclient.network.schemas.GetProgramDetailsResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ProgramApiService {
    @GET("program/{programId}")
    suspend fun getProgramDetails(@Path("programId") programId: String) : GetProgramDetailsResponse

    @GET("faculty/courses-list")
    suspend fun getFacultyCourses() : GetFacultyCoursesResponse
}