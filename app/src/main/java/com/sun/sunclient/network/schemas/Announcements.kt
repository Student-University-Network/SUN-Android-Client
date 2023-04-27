package com.sun.sunclient.network.schemas

data class Announcement(
    val announcementId: String,
    val title: String,
    val content: String,
    val programId: String?,
    val userId: String,
    val programName: String?,
    val announcer: String,
)

data class GetAnnouncementsResponse(
    val status: String,
    val data: GetAnnouncementsData,
)

data class GetAnnouncementsData(
    val announcements: List<Announcement>
)

data class GetAnnouncementProgramListResponse(
    val status: String,
    val data: GetAnnouncementProgramData,
)

data class AnnouncementProgram(
    val programId: String,
    val programName: String,
    val tag: String
)

data class GetAnnouncementProgramData(
    val programs: List<AnnouncementProgram>
)

data class PostAnnouncementInput(
    val title: String,
    val content: String,
    val programId: String?
)