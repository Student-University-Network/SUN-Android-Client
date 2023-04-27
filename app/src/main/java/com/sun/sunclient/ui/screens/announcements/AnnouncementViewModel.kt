package com.sun.sunclient.ui.screens.announcements

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sun.sunclient.MyEvents
import com.sun.sunclient.network.repository.AnnouncementRepository
import com.sun.sunclient.network.schemas.Announcement
import com.sun.sunclient.network.schemas.AnnouncementProgram
import com.sun.sunclient.network.schemas.PostAnnouncementInput
import com.sun.sunclient.utils.AppEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnnouncementViewModel @Inject constructor(private val announcementRepository: AnnouncementRepository) :
    ViewModel() {
    val TAG = "AnnouncementViewModel"

    var announcementsList by mutableStateOf<List<Announcement>>(ArrayList())
    var programList by mutableStateOf<List<AnnouncementProgram>>(ArrayList())

    init {
        viewModelScope.launch {
            getAnnouncements()
        }
    }

    fun postAnnouncement(title: String, content: String, programId: String? = null, done: () -> Unit) {
        viewModelScope.launch {
            announcementRepository.setAnnouncements(
                PostAnnouncementInput(
                    title,
                    content,
                    programId
                )
            )
            MyEvents.eventFlow.send(AppEvent.SnackBar("Posted new notice"))
            getAnnouncements()
        }
    }

    fun getAnnouncements() {
        viewModelScope.launch {
            announcementRepository.getAnnouncements()
            announcementsList = announcementRepository.announcementsData
        }
    }

    fun getProgramList() {
        viewModelScope.launch {
            val list = announcementRepository.getAnnouncementsProgramList()
            programList = list
        }
    }
}