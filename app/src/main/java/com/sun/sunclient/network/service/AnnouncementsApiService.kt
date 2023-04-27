package com.sun.sunclient.network.service

import com.sun.sunclient.network.schemas.GetAnnouncementProgramListResponse
import com.sun.sunclient.network.schemas.GetAnnouncementsResponse
import com.sun.sunclient.network.schemas.PostAnnouncementInput
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AnnouncementsApiService {

    @GET("announcement/list")
    suspend fun getAnnouncements() : GetAnnouncementsResponse

    @GET("announcement/program-list")
    suspend fun getAnnouncementProgramsList() : GetAnnouncementProgramListResponse

    @POST("announcement/announce")
    suspend fun postAnnouncement(@Body payload: PostAnnouncementInput)
}